package plugin;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

import scout.Action;
import scout.AppState;
import scout.GoHomeAction;
import scout.LeftClickAction;
import scout.MouseScrollAction;
import scout.RemoveAction;
import scout.Scout;
import scout.StateController;
import scout.StateController.Mode;
import scout.StateController.SessionState;
import scout.TypeAction;
import scout.Widget;
import scout.Widget.WidgetStatus;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;
import scout.WidgetMatch;

public class SeleniumPlugin
{
	public static final String DATA_FILEPATH = "data";
	public static final String COOKIES_FILE = "cookies";

	private static WebDriver webDriver=null;
	private static BufferedImage latestScreenCapture=null;
	private static boolean isTakingCapture=false;
	private static AppState actualState=null;
	private static Widget widgetToRemove=null;
	private static long widgetRemoveUntilTime=0;
	private static boolean isSwitchingFrame=false;
	private static long startTypeTime=0;

	private static String[] seleniumKeyTag=null;
	private static String[] seleniumKeyValue=null;
	
	private static Session thisSession = null;

	public void startSession()
	{
		System.out.println("Starting webdriver");
		webDriver=getWebDriver(StateController.getProductView());
		System.out.println("we're here2");

		if(webDriver!=null)
		{
		  	int width=StateController.getProductViewWidth();
		  	int height=StateController.getProductViewHeight();
		  	System.out.println(width + " - " + height);
		  	webDriver.manage().window().setSize(new Dimension(width, height));
//			webDriver.manage().window().maximize();
			webDriver.get(StateController.getHomeLocator());
			StateController.setSessionState(SessionState.RUNNING);
			actualState=StateController.getCurrentState();
			
			//TODO: check + debug all'avvio
			thisSession = new Session(StateController.getHomeLocator(),StateController.getTesterName());
			if(actualState.getMetadata("cookies")!=null)
			{
				loadCookies();
			}
			thisSession.startSessionTiming();
		}
		else {
			
			System.out.println("webdriver is null");
		}
	}

	public void stopSession()
	{
		//long endTime = System.currentTimeMillis();
		thisSession.stopSessionTiming();
		thisSession.computeTimeSession();
		
		if(webDriver!=null)
		{
			webDriver.quit();
		}
		
		thisSession.computeStats();
		//thisSession.printTree();
		
		System.out.println(thisSession.getStringTiming());
		thisSession.getRoot().printTiming();
	}
	

	public void storeHomeState()
	{
		saveCookies();
		if(actualState!=null)
		{
			actualState.putMetadata("cookies", true);
		}
	}

	public String[] getProductViews()
	{
		return new String[] {"Chrome", "Firefox"};
	}
	
	public BufferedImage getCapture()
	{
		if(StateController.isOngoingSession())
		{
			BufferedImage image=createBrowserScreenCapture();
			if(image!=null)
			{
				// Update the status of all widgets
				verifyAndReplaceWidgets();
			}
			return image;
		}
		else
		{
			return null;
		}
	}

	public void changeState()
	{
		actualState=StateController.getCurrentState();
		if(actualState.getMetadata("cookies")!=null)
		{
			loadCookies();
		}
	}

	public void performAction(Action action)
	{
		if(!StateController.isRunningSession() || (StateController.isToolbarVisible() && StateController.getMode()==Mode.MANUAL && !"AugmentedToolbar".equalsIgnoreCase(action.getCreatedByPlugin())))
		{
			// Only perform actions during a running session and not showing toolbar
			return;
		}
		try
		{
			if(action instanceof MouseScrollAction)
			{
				MouseScrollAction mouseScrollAction=(MouseScrollAction)action;
				StateController.setSelectedWidgetNo(StateController.getSelectedWidgetNo()+mouseScrollAction.getRotation());
				return;
			}

			if(action instanceof GoHomeAction)
			{
				webDriver.get(StateController.getHomeLocator());
				Widget goHomeWidget=new Widget();
				goHomeWidget.setWidgetType(WidgetType.ACTION);
				goHomeWidget.setWidgetSubtype(WidgetSubtype.GO_HOME_ACTION);
				goHomeWidget.setLocationArea(new Rectangle(10, 10, 80, 40));
				StateController.insertWidget(goHomeWidget, StateController.getStateTree());
				thisSession.newInteraction(GamificationUtils.logInformation(goHomeWidget));
				thisSession.goHome();
				return;
			}

			if(action instanceof RemoveAction)
			{
				RemoveAction removeAction=(RemoveAction)action;
				Widget locatedWidget=StateController.getWidgetAt(actualState, removeAction.getLocation());
				if(locatedWidget!=null)
				{
					actualState.removeWidget(locatedWidget);
				}
				return;
			}

			if(action instanceof TypeAction)
			{
				TypeAction typeAction=(TypeAction)action;
				KeyEvent keyEvent=typeAction.getKeyEvent();
				int keyCode = keyEvent.getKeyCode();
				char keyChar = keyEvent.getKeyChar();

				if(keyCode==KeyEvent.VK_ENTER)
				{
					// Send keyboardInput to selected widget (if any)
					StateController.addKeyboardInput("[ENTER]");
					performTypeAction(typeAction.getLocation());
					Widget locatedWidget=StateController.getWidgetAt(actualState, typeAction.getLocation());
					thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
				}
				else if(keyCode==KeyEvent.VK_DELETE || keyCode==KeyEvent.VK_BACK_SPACE)
				{
					if(StateController.getKeyboardInput().length()>0)
					{
						StateController.removeLastKeyboardInput();
					}
					else
					{
						// Remove selected widget
						Widget locatedWidget=StateController.getWidgetAt(actualState, typeAction.getLocation());
						if(locatedWidget!=null)
						{
							StateController.displayMessage("Press y to remove the selected widget");
							// 5 seconds to press y
							widgetToRemove=locatedWidget;
							widgetRemoveUntilTime=System.currentTimeMillis()+5000;
						}
					}
				}
				else if(keyChar=='y' && widgetToRemove!=null && widgetRemoveUntilTime>System.currentTimeMillis())
				{
					actualState.removeWidget(widgetToRemove);
					widgetToRemove=null;
					StateController.displayMessageHide();
				}
				else if(keyCode==KeyEvent.VK_ESCAPE)
				{
					StateController.clearKeyboardInput();
				}
				else if(keyCode==KeyEvent.VK_SPACE)
				{
					StateController.addKeyboardInput(" ");
				}
				else if(keyCode==KeyEvent.VK_UP)
				{
					StateController.setSelectedWidgetNo(StateController.getSelectedWidgetNo()-1);
				}
				else if(keyCode==KeyEvent.VK_DOWN)
				{
					StateController.setSelectedWidgetNo(StateController.getSelectedWidgetNo()+1);
				}
				else
				{
					if(StateController.getKeyboardInput().length()==0)
					{
						// First typed char - remember the time
						startTypeTime=System.currentTimeMillis();
					}
					StateController.addKeyboardInput(getKeyText(keyChar));
				}
			}

			if(action instanceof LeftClickAction)
			{
				// Left mouse click on a widget
				LeftClickAction leftClickAction=(LeftClickAction)action;
				Widget locatedWidget=StateController.getWidgetAt(actualState, leftClickAction.getLocation());
				if(locatedWidget!=null)
				{
					// Located a widget
					if(locatedWidget.getWidgetType()==WidgetType.ACTION)
					{
						if(locatedWidget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION)
						{
							thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
							if(StateController.getKeyboardInput().length()>0)
							{
								// We have keyboard input to type
								performTypeAction(leftClickAction.getLocation());
							}
							else
							{
								// Click on a type action
								WebElement locatedElement=findWebElement(locatedWidget);
								if(locatedElement!=null)
								{
									if(locatedWidget.getText()!=null)
									{
										// Type text
										typeSelenium(locatedElement, locatedWidget.getText());
									}
									if(locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN || locatedWidget.getWidgetVisibility()==WidgetVisibility.SUGGESTION)
									{
										locatedWidget.setWidgetVisibility(WidgetVisibility.VISIBLE);
										locatedWidget.setComment(null);
										StateController.insertWidget(locatedWidget, locatedWidget.getNextState());
									}
									else
									{
										// Perform the type action
										StateController.performWidget(locatedWidget);
									}
								}
							}
						}
						else if(locatedWidget.getWidgetSubtype()==WidgetSubtype.LEFT_CLICK_ACTION)
						{
							if(StateController.getKeyboardInput().length()>0)
							{
								// Keyboard input on visible widget - add a comment
								locatedWidget.setComment(StateController.getKeyboardInput().trim());
								StateController.clearKeyboardInput();
							}
							Integer frameNo=(Integer)locatedWidget.getMetadata("frame_no");
							if(frameNo!=null)
							{
								isSwitchingFrame=true;
								webDriver.switchTo().frame(frameNo);
							}
							try
							{
								WebElement locatedElement=findWebElement(locatedWidget);
								if(locatedElement!=null)
								{
									
									thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
									
									// Click element
									clickWebElement(locatedElement);

									if(locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN || locatedWidget.getWidgetVisibility()==WidgetVisibility.SUGGESTION)
									{
										locatedWidget.setWidgetVisibility(WidgetVisibility.VISIBLE);
										locatedWidget.setComment(null);
										StateController.insertWidget(locatedWidget, locatedWidget.getNextState());
									}
									else
									{
										// Perform action on button or link
										StateController.performWidget(locatedWidget);
									}
								}
							}
							catch(Exception e)
							{
							}

							if(frameNo!=null)
							{
								webDriver.switchTo().defaultContent();
								isSwitchingFrame=false;
							}
						}
						else if(locatedWidget.getWidgetSubtype()==WidgetSubtype.SELECT_ACTION)
						{
							thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
							if(StateController.getKeyboardInput().length()>0)
							{
								// Keyboard input on visible widget - add a comment
								locatedWidget.setComment(StateController.getKeyboardInput().trim());
								StateController.clearKeyboardInput();
							}
							WebElement locatedElement=findWebElement(locatedWidget);
							if(locatedElement!=null)
							{
								// Select option
								Select select=new Select(locatedElement);
								if(locatedWidget.getOptionValue()!=null && locatedWidget.getOptionValue().trim().length()>0)
								{
									select.selectByValue(locatedWidget.getOptionValue());
								}
								else if(locatedWidget.getOptionText()!=null)
								{
									select.selectByVisibleText(locatedWidget.getOptionText());
								}

								if(locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN || locatedWidget.getWidgetVisibility()==WidgetVisibility.SUGGESTION)
								{
									locatedWidget.setWidgetVisibility(WidgetVisibility.VISIBLE);
									locatedWidget.setComment(null);
									StateController.insertWidget(locatedWidget, locatedWidget.getNextState());
								}
								else
								{
									// Perform action on button or link
									StateController.performWidget(locatedWidget);
								}
							}
						}
						else if(locatedWidget.getWidgetSubtype()==WidgetSubtype.GO_HOME_ACTION)
						{
							//System.out.println(GamificationUtils.logInformation(locatedWidget));
							
							if(StateController.getKeyboardInput().length()>0 && locatedWidget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
							{
								// Keyboard input on visible widget - add a comment
								locatedWidget.setComment(StateController.getKeyboardInput().trim());
								StateController.clearKeyboardInput();
							}
							else
							{
								// Click on a home action
								webDriver.get(StateController.getHomeLocator());

								thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
								thisSession.goHome();
								
								if(locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN || locatedWidget.getWidgetVisibility()==WidgetVisibility.SUGGESTION)
								{
									locatedWidget.setWidgetVisibility(WidgetVisibility.VISIBLE);
									locatedWidget.setComment(null);
									StateController.insertWidget(locatedWidget, locatedWidget.getNextState());
								}
								else
								{
									// Perform action on button or link
									StateController.performWidget(locatedWidget);
								}
							}
						}
					}
					else if(locatedWidget.getWidgetType()==WidgetType.CHECK)
					{
						if(StateController.getKeyboardInput().length()>0 && locatedWidget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
						{
							if(StateController.getKeyboardInput().indexOf("{")>=0 && StateController.getKeyboardInput().indexOf("}")>=0)
							{
								// An expression
								locatedWidget.setValidExpression(StateController.getKeyboardInput());
								//System.out.println(GamificationUtils.logInformation(locatedWidget));
								thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
								//System.out.println("The input was a correct expression: "+ StateController.getKeyboardInput());
							}
							else
							{
								// Report an issue
								createIssue(locatedWidget, StateController.getKeyboardInput());
								thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
								//System.out.println(GamificationUtils.logInformation(locatedWidget));
								//System.out.println("The input was interpreted as an issue: "+ StateController.getKeyboardInput());
							}
							StateController.clearKeyboardInput();
						}
						else
						{
							if(locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN)
							{
								locatedWidget.setWidgetVisibility(WidgetVisibility.VISIBLE);
								locatedWidget.setCreatedBy(StateController.getTesterName());
								locatedWidget.setCreatedDate(new Date());
								locatedWidget.setCreatedProductVersion(StateController.getProductVersion());
								//System.out.println(GamificationUtils.logInformation(locatedWidget));
								thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
								//System.out.println("You clicked on a hidden widget: "+ StateController.getKeyboardInput());
							}
						}
					}
					else if(locatedWidget.getWidgetType()==WidgetType.ISSUE)
					{
						//click on an existing issue, transforming it to a check
						if(locatedWidget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
						{
							locatedWidget.setWidgetType(WidgetType.CHECK);
							locatedWidget.setResolvedBy(StateController.getTesterName());
							locatedWidget.setResolvedDate(new Date());
							locatedWidget.setResolvedProductVersion(StateController.getProductVersion());
							Widget matchingWidget=(Widget)locatedWidget.getMetadata("matching_widget");
							if(matchingWidget!=null)
							{
								String matchingText=(String)matchingWidget.getMetadata("text");
								if(matchingText!=null)
								{
									locatedWidget.setValidExpression("{text} = "+matchingText);
								}
							}
							StateController.clearKeyboardInput();
							thisSession.newInteraction(GamificationUtils.logInformation(locatedWidget));
							//System.out.println(GamificationUtils.logInformation(locatedWidget));
						}
					}
				}
				return;
			}
		}
		catch (Exception e)
		{
			return;
		}
		
		return;
	}

	private void performTypeAction(Point location)
	{
		Widget existingTypeWidget=getTypeWidget(location, StateController.getKeyboardInput());
		if(existingTypeWidget!=null)
		{
			// Same text entered as existing widget - perform that widget again
			WebElement locatedElement=findWebElement(existingTypeWidget);
			if(locatedElement!=null)
			{
				typeSelenium(locatedElement, StateController.getKeyboardInput());
				StateController.clearKeyboardInput();
				StateController.performWidget(existingTypeWidget);
				return;
			}
		}
		else
		{
			// Not an identical existing widget
			existingTypeWidget=getTypeWidget(location);
			if(existingTypeWidget!=null)
			{
				// There is a type widget
				Widget insertTypeWidget=new Widget(existingTypeWidget);
				// Set the new text
				insertTypeWidget.setText(StateController.getKeyboardInput());
				// Find the element
				WebElement locatedElement=findWebElement(insertTypeWidget);
				if(locatedElement!=null)
				{
					// Type the text
					typeSelenium(locatedElement, StateController.getKeyboardInput());
					StateController.clearKeyboardInput();
					// Insert the new widget
					StateController.insertWidget(insertTypeWidget);
				}
			}
			else
			{
				// No visible or suggested type action found at that location
				Widget locatedWidget=StateController.getWidgetAt(actualState, location);
				if(locatedWidget!=null)
				{
					if(locatedWidget.getWidgetType()==WidgetType.ACTION && locatedWidget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION && locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN)
					{
						// Found a hidden type action
						WebElement locatedElement=findWebElement(locatedWidget);
						if(locatedElement!=null)
						{
							typeSelenium(locatedElement, StateController.getKeyboardInput());
							
							
							locatedWidget.setText(StateController.getKeyboardInput());
							StateController.clearKeyboardInput();
							StateController.insertWidget(locatedWidget);
						}
					}
					else if(locatedWidget.getWidgetType()==WidgetType.ACTION)
					{
						if(StateController.getKeyboardInput().length()>0 && locatedWidget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
						{
							// Keyboard input on visible widget - add a comment
							if(StateController.getKeyboardInput().endsWith("[ENTER]"))
							{
								StateController.removeLastKeyboardInput();
							}
							locatedWidget.setComment(StateController.getKeyboardInput().trim());
							StateController.clearKeyboardInput();
						}
					}
					else if(locatedWidget.getWidgetType()==WidgetType.CHECK)
					{
						if(StateController.getKeyboardInput().length()>0 && locatedWidget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
						{
							// Report an issue
							if(StateController.getKeyboardInput().endsWith("[ENTER]"))
							{
								StateController.removeLastKeyboardInput();
							}
							if(StateController.getKeyboardInput().indexOf("{")>=0 && StateController.getKeyboardInput().indexOf("}")>=0)
							{
								// An expression
								locatedWidget.setValidExpression(StateController.getKeyboardInput());
							}
							else
							{
								// Report an issue
								createIssue(locatedWidget, StateController.getKeyboardInput());
							}
							StateController.clearKeyboardInput();
						}
					}
				}
			}
		}
	}

	private void createIssue(Widget widget, String reportedText)
	{
		widget.setWidgetType(WidgetType.ISSUE);
		widget.setWidgetVisibility(WidgetVisibility.VISIBLE);
		widget.setReportedText(reportedText);
		widget.setReportedBy(StateController.getTesterName());
		widget.setReportedDate(new Date());
		widget.setReportedProductVersion(StateController.getProductVersion());
		Widget matchingWidget=(Widget)widget.getMetadata("matching_widget");
		if(matchingWidget!=null)
		{
			String matchingText=(String)matchingWidget.getMetadata("text");
			if(matchingText!=null)
			{
				widget.setValidExpression("{text} = "+matchingText);
			}
		}
		if(startTypeTime>0)
		{
			long deltaTime=System.currentTimeMillis()-startTypeTime;
			startTypeTime=0;
			widget.putMetadata("report_issue_time", deltaTime);
		}
	}
	
	/**
	 * @param typeAction
	 * @return An existing type widget at location and with the same text or null
	 */
	private Widget getTypeWidget(Point location, String text)
	{
		List<Widget> locatedWidgets=StateController.getWidgetsAt(location);
		for(Widget locatedWidget:locatedWidgets)
		{
			if(locatedWidget.getWidgetType()==WidgetType.ACTION && locatedWidget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION && locatedWidget.getWidgetVisibility()!=WidgetVisibility.HIDDEN)
			{
				// Found a non hidden type action
				if(text!=null && text.equals(locatedWidget.getText()))
				{
					// Same text
					return locatedWidget;
				}
			}
		}
		return null;
	}

	/**
	 * @param location
	 * @return An existing type widget at location or null
	 */
	private Widget getTypeWidget(Point location)
	{
		List<Widget> locatedWidgets=StateController.getWidgetsAt(location);
		for(Widget locatedWidget:locatedWidgets)
		{
			if(locatedWidget.getWidgetType()==WidgetType.ACTION && locatedWidget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION && locatedWidget.getWidgetVisibility()!=WidgetVisibility.HIDDEN)
			{
				// Found a non hidden type action
				return locatedWidget;
			}
		}
		return null;
	}

	/**
	 * Verify that visible widgets are still valid and replace hidden widgets
	 */
	private synchronized void verifyAndReplaceWidgets()
	{
		try
		{
			List<Widget> availableWidgets=getAvailableWidgets();
			
			if(availableWidgets==null)
			{
				return;
			}
			List<Widget> hiddenAvailableWidgets=new ArrayList<Widget>();
			List<Widget> nonHiddenWidgets=actualState.getNonHiddenWidgets();
			for(Widget widget:availableWidgets)
			{
				hiddenAvailableWidgets.add(widget);
			}
			for(Widget widget:nonHiddenWidgets)
			{
				if(actualState!=StateController.getCurrentState())
				{
					// The state has been changed - abort
					return;
				}

				if(widget.getWidgetSubtype()==WidgetSubtype.GO_HOME_ACTION)
				{
					// The Go Home widget is always located
					widget.setWidgetStatus(WidgetStatus.LOCATED);
				}
				else
				{
					WidgetMatch widgetMatch=findBestMatchingWidget(widget, availableWidgets);
					if(widgetMatch!=null)
					{
						Widget matchingWidget=widgetMatch.getWidget2();

						widget.putMetadata("matching_widget", matchingWidget);

						if(widget.getValidExpression()!=null)
						{
							if(matchingWidget.evaluateExpression(widget.getValidExpression()))
							{
								// Expression is valid
								widget.setWidgetStatus(WidgetStatus.VALID);
								widget.setComment(widget.getValidExpression());
							}
							else
							{
								// Expression not valid
								widget.setWidgetStatus(WidgetStatus.LOCATED);
								String comment=matchingWidget.getReplacedParameters(widget.getValidExpression());
								if(comment.length()>60)
								{
									comment=widget.getValidExpression();
								}
								widget.setComment(comment);
							}
						}
						else
						{
							widget.setWidgetStatus(WidgetStatus.LOCATED);
						}
						hiddenAvailableWidgets.remove(widget);
					}
					else
					{
						// Could not locate widget
						widget.setWidgetStatus(WidgetStatus.UNLOCATED);
					}
				}
			}

			actualState.replaceHiddenWidgets(hiddenAvailableWidgets, "SeleniumPlugin");
			
			int widthW=StateController.getProductViewWidth();
		  	int heightW=StateController.getProductViewHeight();
		  	
		  	List<Widget> l = hiddenAvailableWidgets.stream()
		  			.filter( w -> (w.getLocationArea().y + w.getLocationArea().height < heightW && w.getLocationArea().x + w.getLocationArea().width < widthW))
		  			.filter(w -> (w.getLocationArea().height > 1 && w.getLocationArea().width > 1))
		  			.collect(Collectors.toList());
		  	
			
			//System.out.println("La lista filtrata ha " + l.size() + " elementi");
			//System.out.println("La lista non filtrata ha " + hiddenAvailableWidgets.size() + " elementi");
		  	
			thisSession.setActiveWidgetCurrentPage(nonHiddenWidgets.size());
			//we use only the widgets that fits in the page
			thisSession.setTotalWidgetCurrentPage(l.size());
		}
		catch(Exception e)
		{
		}
	}

	/**
	 * @param widget
	 * @param availableWidgets
	 * @return The best matching widget in availableWidgets
	 */
	private WidgetMatch findBestMatchingWidget(Widget widget, List<Widget> availableWidgets)
	{
		int bestScore=0;
		WidgetMatch bestWidgetMatch=null;
		
		for(Widget availableWidget:availableWidgets)
		{
			WidgetMatch widgetMatch=matchScore(widget, availableWidget);
			if(widgetMatch!=null)
			{
				if(widgetMatch.getScore()>bestScore)
				{
					bestScore=widgetMatch.getScore();
					bestWidgetMatch=widgetMatch;
				}
			}
		}
		
		if(bestScore<40)
		{
			// Match is not good enough
			return null;
		}

		return bestWidgetMatch;
	}

	/**
	 * Used to find the WebElement that best represent the widget
	 * 
	 * @param widget
	 * @param availableElements
	 * @return The best matching element in availableElements
	 */
	private WebElement findBestMatchingElement(Widget widget, List<WebElement> availableElements)
	{
		int bestScore=0;
		WebElement bestWidget=null;

		// Get the corresponding widgets
		//It's a parsing form the JSON object in exam
		List<Widget> availableWidgets=getWidgets(availableElements);

		for(int i=0; i<availableElements.size(); i++)
		{
			WebElement availableElement=availableElements.get(i);
			Widget availableWidget=getWidgetByIndex(availableWidgets, i);
			if(availableWidget!=null)
			{
				WidgetMatch widgetMatch=matchScore(widget, availableWidget);
				if(widgetMatch!=null)
				{
					if(widgetMatch.getScore()>bestScore)
					{
						bestScore=widgetMatch.getScore();
						bestWidget=availableElement;
					}
				}
			}
		}
		
		if(bestScore<40)
		{
			// Match is not good enough
			return null;
		}
		
		return bestWidget;
	}
	
	private Widget getWidgetByIndex(List<Widget> widgets, int index)
	{
		for(Widget widget:widgets)
		{
			Long i=(Long)widget.getMetadata("index");
			if(index==i)
			{
				return widget;
			}
		}
		return null;
	}
	
	private boolean isClickableWidget(Widget widget)
	{
		String tag=(String)widget.getMetadata("tag");
		
		if(tag!=null)
		{
			if(tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("a"))
			{
				return true;
			}

			String type=(String)widget.getMetadata("type");
			if(type!=null)
			{
				if(tag.equalsIgnoreCase("input") && type.equalsIgnoreCase("submit"))
				{
					return true;
				}
				if(tag.equalsIgnoreCase("input") && type.equalsIgnoreCase("reset"))
				{
					return true;
				}
				if(tag.equalsIgnoreCase("input") && type.equalsIgnoreCase("radio"))
				{
					return true;
				}
				if(tag.equalsIgnoreCase("input") && type.equalsIgnoreCase("checkbox"))
				{
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean isSelectWidget(Widget widget)
	{
		String tag=(String)widget.getMetadata("tag");
		
		if(tag==null)
		{
			return false;
		}
		
		if(tag.equalsIgnoreCase("select"))
		{
			return true;
		}
		
		return false;
	}

	private boolean isCheckWidget(Widget widget)
	{
		String tag=(String)widget.getMetadata("tag");
		
		if(tag==null)
		{
			return false;
		}
		
		if(tag.equalsIgnoreCase("h1") || tag.equalsIgnoreCase("h2") || tag.equalsIgnoreCase("h3") || tag.equalsIgnoreCase("h4") || tag.equalsIgnoreCase("h5") || tag.equalsIgnoreCase("li") || tag.equalsIgnoreCase("span") || tag.equalsIgnoreCase("div") || tag.equalsIgnoreCase("p") || tag.equalsIgnoreCase("td") || tag.equalsIgnoreCase("th"))
		{
			return true;
		}

		return false;
	}
	
	private boolean isTypeWidget(Widget widget)
	{
		String tag=(String)widget.getMetadata("tag");
		String type=(String)widget.getMetadata("type");
		
		if(tag==null)
		{
			return false;
		}

		if(tag.equalsIgnoreCase("textarea"))
		{
			return true;
		}

		if(type==null)
		{
			return false;
		}

		if(tag.equalsIgnoreCase("input"))
		{
			if(type.equalsIgnoreCase("text") || type.equalsIgnoreCase("color") || type.equalsIgnoreCase("date") || type.equalsIgnoreCase("datetime-local") || type.equalsIgnoreCase("email"))
			{
				return true;
			}
			if(type.equalsIgnoreCase("month") || type.equalsIgnoreCase("number") || type.equalsIgnoreCase("range") || type.equalsIgnoreCase("search") || type.equalsIgnoreCase("tel"))
			{
				return true;
			}
			if(type.equalsIgnoreCase("time") || type.equalsIgnoreCase("url") || type.equalsIgnoreCase("week") || type.equalsIgnoreCase("password"))
			{
				return true;
			}
		}

		return false;
	}

	private boolean isFrame(Widget widget)
	{
		String tag=(String)widget.getMetadata("tag");
		
		if(tag==null)
		{
			return false;
		}
		
		if(tag.equalsIgnoreCase("frame") || tag.equalsIgnoreCase("iframe"))
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Calculate a weighted match percent
	 * @param widget1
	 * @param widget2
	 * @return A match or null
	 */
	private WidgetMatch matchScore(Widget widget1, Widget widget2)
	{
		String tag1=(String)widget1.getMetadata("tag");
		String className1=(String)widget1.getMetadata("class");
		String type1=(String)widget1.getMetadata("type");
		String name1=(String)widget1.getMetadata("name");
		String id1=(String)widget1.getMetadata("id");
		String value1=(String)widget1.getMetadata("value");
		String href1=(String)widget1.getMetadata("href");
		String text1=(String)widget1.getMetadata("text");
		Rectangle locationArea1=widget1.getLocationArea();

		String tag2=(String)widget2.getMetadata("tag");
		String className2=(String)widget2.getMetadata("class");
		String type2=(String)widget2.getMetadata("type");
		String name2=(String)widget2.getMetadata("name");
		String id2=(String)widget2.getMetadata("id");
		String value2=(String)widget2.getMetadata("value");
		String href2=(String)widget2.getMetadata("href");
		String text2=(String)widget2.getMetadata("text");
		Rectangle locationArea2=widget2.getLocationArea();

		if(tag1==null || tag2==null || !tag1.equalsIgnoreCase(tag2))
		{
			// No the same type of element
			return null;
		}
		
		int score=0;
		int maxScore=0;

		if("a".equalsIgnoreCase(tag1) && "a".equalsIgnoreCase(tag2))
		{
			if(bothContainsValue(href1, href2))
			{
				maxScore+=100;
				if(href1.equals(href2))
				{
					score+=100;
				}
			}
			if(bothContainsValue(text1, text2))
			{
				if(text1.length()>5)
				{
					// Long text
					maxScore+=100;
					if(text1.equals(text2))
					{
						score+=100;
					}
					else if(text1.equalsIgnoreCase(text2))
					{
						score+=50;
					}
				}
				else
				{
					// Short text
					maxScore+=50;
					if(text1.equals(text2))
					{
						score+=50;
					}
					else if(text1.equalsIgnoreCase(text2))
					{
						score+=30;
					}
				}
			}
		}
		else if(isCheckWidget(widget1) && isCheckWidget(widget2))
		{
			if(bothContainsValue(text1, text2))
			{
				if(text1.length()>5)
				{
					// Long text
					maxScore+=100;
					if(text1.equals(text2))
					{
						score+=100;
					}
					else if(text1.equalsIgnoreCase(text2))
					{
						score+=50;
					}
				}
				else
				{
					// Short text
					maxScore+=50;
					if(text1.equals(text2))
					{
						score+=50;
					}
					else if(text1.equalsIgnoreCase(text2))
					{
						score+=30;
					}
				}
			}
		}
		if(bothContainsValue(id1, id2))
		{
			maxScore+=100;
			if(id1.equals(id2))
			{
				score+=100;
			}
		}
		if(bothContainsValue(name1, name2))
		{
			maxScore+=100;
			if(name1.equals(name2))
			{
				score+=100;
			}
		}
		if(bothContainsValue(value1, value2))
		{
			maxScore+=70;
			if(value1.equals(value2))
			{
				score+=70;
			}
		}
		if(bothContainsValue(className1, className2))
		{
			maxScore+=30;
			if(className1.equals(className2))
			{
				score+=30;
			}
		}
		if(bothContainsValue(type1, type2))
		{
			maxScore+=20;
			if(type1.equals(type2))
			{
				score+=20;
			}
		}
		if(locationArea1!=null && locationArea2!=null)
		{
			maxScore+=20;
			if(Math.abs(locationArea1.getX()-locationArea2.getX())<5 && Math.abs(locationArea1.getY()-locationArea2.getY())<5)
			{
				score+=20;
			}
			else if(Math.abs(locationArea1.getX()-locationArea2.getX())<5 || Math.abs(locationArea1.getY()-locationArea2.getY())<5)
			{
				score+=10;
			}
			maxScore+=10;
			if(Math.abs(locationArea1.getWidth()-locationArea2.getWidth())<5 && Math.abs(locationArea1.getHeight()-locationArea2.getHeight())<5)
			{
				score+=20;
			}
			else if(Math.abs(locationArea1.getWidth()-locationArea2.getWidth())<5 || Math.abs(locationArea1.getHeight()-locationArea2.getHeight())<5)
			{
				score+=10;
			}
		}

		if(maxScore==0)
		{
			return null;
		}
		
		return new WidgetMatch(widget1, widget2, score, maxScore);
	}

	private boolean bothContainsValue(String value1, String value2)
	{
		if(value1!=null && value2!=null && value1.trim().length()>0 && value2.trim().length()>0)
		{
			return true;
		}
		return false;
	}
	
  private WebDriver getWebDriver(String browser)
  {
  	boolean headlessBrowser=StateController.isHeadlessBrowser();
//  	String seleniumWindowSize=StateController.getProperty("selenium_window_size", "1920x1080");

		if("Firefox".equalsIgnoreCase(browser))
		{
			String driverName=getGeckoDriverName();
			if(driverName!=null)
			{
				File driverFile=new File(getPath(), driverName);
				String driverPath=driverFile.getAbsolutePath();
				System.setProperty("webdriver.gecko.driver", driverPath);
				FirefoxOptions options = new FirefoxOptions();
				if(headlessBrowser)
				{
	        options.addArguments("--headless");
				}
//        options.addArguments("--window-size="+seleniumWindowSize);
				return new FirefoxDriver(options);
			}
		}
		else if("Chrome".equalsIgnoreCase(browser))
		{
			String driverName=getChromeDriverName();
			if(driverName!=null)
			{
				File driverFile=new File(getPath(), driverName);
				String driverPath=driverFile.getAbsolutePath();
				System.setProperty("webdriver.chrome.driver", driverPath);
				ChromeOptions options = new ChromeOptions();
				if(headlessBrowser)
				{
	        options.addArguments("headless");
				}
//        options.addArguments("window-size="+seleniumWindowSize);
				return new ChromeDriver(options);
			}
		}

		return null;
  }

	private String getChromeDriverName()
	{
		String osName = System.getProperty("os.name");
		boolean isWin = osName.startsWith("Windows");
		boolean isOSX = osName.startsWith("Mac");
		boolean isLinux = osName.indexOf("nux")>=0;
		if(isWin)
		{
			return "bin/chromedriver.exe";
		}
		else if(isOSX)
		{
			return "bin/chromedriver_mac";
		}
		else if(isLinux)
		{
			return "bin/chromedriver_linux";
		}
		else
		{
			return null;
		}
	}
	
	private String getGeckoDriverName()
	{
		String osName = System.getProperty("os.name");
		boolean isWin = osName.startsWith("Windows");
		boolean isOSX = osName.startsWith("Mac");
		boolean isLinux = osName.indexOf("nux")>=0;
		if(isWin)
		{
			return "bin/geckodriver.exe";
		}
		else if(isOSX)
		{
			return "bin/geckodriver_mac";
		}
		else if(isLinux)
		{
			return "bin/geckodriver_linux";
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return The file path to the scout jar
	 */
	private String getPath()
	{
		String path = Scout.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = path;
		try
		{
			decodedPath = URLDecoder.decode(path, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}

		String absolutePath = decodedPath.substring(0, decodedPath.lastIndexOf("/")) + "/";
		return absolutePath;
	}

	/**
	 * @return A capture from a local or remote browser.
	 */
	private BufferedImage createBrowserScreenCapture()
	{
		if(webDriver==null)
		{
			return null;
		}
		try
		{
			if(isTakingCapture && latestScreenCapture!=null)
			{
				return null;
			}
			isTakingCapture=true;
			TakesScreenshot takesScreenshot=null;
			if(webDriver instanceof TakesScreenshot)
			{
				takesScreenshot=(TakesScreenshot)webDriver;
			}
			else if(webDriver.getClass()==RemoteWebDriver.class)
			{
				takesScreenshot=(TakesScreenshot)new Augmenter().augment(webDriver);
			}
			else
			{
				return null;
			}

			File source=takesScreenshot.getScreenshotAs(OutputType.FILE);
			latestScreenCapture = ImageIO.read(source);

			isTakingCapture=false;
      return latestScreenCapture;
		}
		catch (Exception e)
		{
			isTakingCapture=false;
			return null;
		}
	}

	/**
	 * @return All widgets available on the current page
	 */
	private List<Widget> getAvailableWidgets()
	{
		List<Widget> availableWidgets=new ArrayList<Widget>();
		List<Widget> checkWidgets=new ArrayList<Widget>();
		int frameNo=0;
		
		if(webDriver!=null)
		{
			try
			{
				JavascriptExecutor executor = (JavascriptExecutor) webDriver;
				executor.executeScript("window.scrollTo(0, 0)");
				Object object=executor.executeScript("var result = []; " +
					"function getXPosition(el) {var xPos = 0; while (el) {xPos += (el.offsetLeft - el.scrollLeft + el.clientLeft); el = el.offsetParent;} return xPos;} " +
					"function getYPosition(el) {var yPos = 0; while (el) {yPos += (el.offsetTop - el.scrollTop + el.clientTop); el = el.offsetParent;} return yPos;} " +
					"function getMaxWidth(el) {var children = el.children;var max=el.offsetWidth; for (var i = 0; i < children.length; i++) {var child = children[i]; if (child.offsetWidth>max) max=child.offsetWidth;} return max;} " +
					"function getMaxHeight(el) {var children = el.children;var max=el.offsetHeight; for (var i = 0; i < children.length; i++) {var child = children[i]; if (child.offsetHeight>max) max=child.offsetHeight;} return max;} " +
					"function elementIsVisible(el) {if (getComputedStyle(el).visibility === 'hidden') return false; return true;} " +
					"var all = document.querySelectorAll('input, textarea, button, select, a, h1, h2, h3, h4, h5, li, span, div, p, td, th, frame, iframe'); " +
					"for (var i=0, max=all.length; i < max; i++) { " +
					"    if (elementIsVisible(all[i])) result.push({'tag': all[i].tagName, 'class': all[i].getAttribute('class'), 'type': all[i].getAttribute('type'), 'name': all[i].getAttribute('name'), 'id': all[i].getAttribute('id'), 'value': all[i].getAttribute('value'), 'href': all[i].getAttribute('href'), 'text': all[i].textContent, 'x': getXPosition(all[i]), 'y': getYPosition(all[i]), 'width': getMaxWidth(all[i]), 'height': getMaxHeight(all[i])}); " +
					"} " +
					" return JSON.stringify(result); ");

				String json=object.toString();
				JSONParser parser = new JSONParser();
				JSONArray jsonArray = (JSONArray)parser.parse(json);
				
				for(int i=0; i<jsonArray.size(); i++)
				{
					JSONObject jsonObject=(JSONObject)jsonArray.get(i);

					String tag=(String)jsonObject.get("tag");
					String className=(String)jsonObject.get("class");
					String type=(String)jsonObject.get("type");
					String name=(String)jsonObject.get("name");
					String id=(String)jsonObject.get("id");
					String value=(String)jsonObject.get("value");
					String href=(String)jsonObject.get("href");
					String text=(String)jsonObject.get("text");
					Long x=(Long)jsonObject.get("x");
					Long y=(Long)jsonObject.get("y");
					Long width=(Long)jsonObject.get("width");
					Long height=(Long)jsonObject.get("height");

					if(width>0 && height>0)
					{
						Widget widget=new Widget();
						widget.setLocationArea(new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue()));
						
						widget.putMetadata("tag", tag);
						widget.putMetadata("class", className);
						widget.putMetadata("type", type);
						widget.putMetadata("name", name);
						widget.putMetadata("id", id);
						widget.putMetadata("value", value);
						widget.putMetadata("href", href);
						widget.putMetadata("text", text);

						if("div".equalsIgnoreCase(tag))
						{
							if(text!=null && text.trim().length()>0)
							{
								widget.setWidgetType(WidgetType.CHECK);
								widget.setValidExpression("{text} = "+text.trim());
								checkWidgets.add(widget);
			
							}
						}
						else
						{
							if(isCheckWidget(widget))
							{
								if(text!=null && text.trim().length()>0)
								{
									// Add a check
									widget.setWidgetType(WidgetType.CHECK);
									widget.setValidExpression("{text} = "+text.trim());
									availableWidgets.add(widget);
								}
							}
							else if(isTypeWidget(widget))
							{
								widget.setWidgetType(WidgetType.ACTION);
								widget.setWidgetSubtype(WidgetSubtype.TYPE_ACTION);
								widget.setText(text);
								availableWidgets.add(widget);
							}
							else if(isClickableWidget(widget))
							{
								widget.setWidgetType(WidgetType.ACTION);
								widget.setWidgetSubtype(WidgetSubtype.LEFT_CLICK_ACTION);
								availableWidgets.add(widget);
							}
							else if (isSelectWidget(widget))
							{
								widget.setWidgetType(WidgetType.ACTION);
								widget.setWidgetSubtype(WidgetSubtype.SELECT_ACTION);
								WebElement webElement=findWebElement(widget);
								if(webElement!=null)
								{
									Select select=new Select(webElement);
									List<WebElement> options=select.getOptions();
									for(WebElement option:options)
									{
										// Create a new option
										Widget optionWidget=new Widget(widget);
										String optionText=option.getText();
										optionWidget.setText(optionText);
										optionWidget.setOptionText(optionText);
										String optionValue=option.getAttribute("value");
										optionWidget.setOptionValue(optionValue);
										availableWidgets.add(optionWidget);
									}
								}
							}
							else if(isFrame(widget))
							{
								if(isSwitchingFrame)
								{
									// Abort
									return null;
								}
								webDriver.switchTo().frame(frameNo);
								List<Widget> frameWidgets=getAvailableWidgets();
								if(isSwitchingFrame)
								{
									// Abort
									return null;
								}
								webDriver.switchTo().defaultContent();
								for(Widget frameWidget:frameWidgets)
								{
									Rectangle area=frameWidget.getLocationArea();
									frameWidget.setLocationArea(new Rectangle((int)area.getX()+x.intValue(), (int)area.getY()+y.intValue(), (int)area.getWidth(), (int)area.getHeight()));
									frameWidget.putMetadata("frame_no", frameNo);
									frameWidget.putMetadata("location_in_frame", area);
									availableWidgets.add(frameWidget);
								}
								frameNo++;
							}
						}
					}
				}
				
				// Add check widgets if they are not overlapping (smallest first)
				checkWidgets=StateController.sortWidgets(checkWidgets);
				
				//TODO: eventualmente controllare che i widget siano effettivamente visibili
				int widthW=StateController.getProductViewWidth();
			  	int heightW=StateController.getProductViewHeight();
			  	int count = 0;
				for(Widget w:availableWidgets) {
					if(w.getLocationArea().y<heightW && w.getLocationArea().x<widthW) {
						count++;
					}
				}
				
				for(Widget w:checkWidgets) {
					if(w.getLocationArea().y<heightW && w.getLocationArea().x<widthW) {
						count++;
					}
				}
				
				for(Widget w:checkWidgets)
				{
					if(!isOverlapping(availableWidgets, w))
					{
						availableWidgets.add(w);
					}
				}
				
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				return availableWidgets;
			}
		}
			
		return availableWidgets;
	}

	private static boolean isOverlapping(List<Widget> widgets, Widget widget)
	{
		for(Widget w:widgets)
		{
			if(w.getLocationArea().intersects(widget.getLocationArea()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Find a WebElement from a widget
	 * @param widget
	 * @return A WebElement or null
	 */
	private WebElement findWebElement(Widget widget)
	{
		WebElement element=findWebElementInt(widget);

		// Retry to find the web element if needed
		for(int i=0; i<5 && element==null; i++)
		{
			StateController.delay(500);
			element=findWebElementInt(widget);
		}

		if(element==null)
		{
			StateController.displayMessage("Widget is not available right now");
		}

		return element;
	}
	
	private WebElement findWebElementInt(Widget widget)
	{
		if(webDriver==null)
		{
			return null;
		}

		List<WebElement> allElements=new ArrayList<WebElement>();

		String tag=(String)widget.getMetadata("tag");
		if(tag==null)
		{
			return null;
		}
		
		
		//if there's a link
		if("a".equalsIgnoreCase(tag))
		{
			String text=(String)widget.getMetadata("text");
			if(text!=null && text.trim().length()>0)
			{
				try
				{
					List<WebElement> elements=webDriver.findElements(By.linkText(text));
					allElements.addAll(elements);
				}
				catch(Exception e)
				{
				}
			}
			String href=(String)widget.getMetadata("href");
			if(href!=null && href.trim().length()>0)
			{
				try
				{
					List<WebElement> elements=webDriver.findElements(By.cssSelector("a[href='"+href+"']"));
					allElements.addAll(elements);
				}
				catch(Exception e)
				{
				}
			}
		}

		//get the id of the widget
		String id=(String)widget.getMetadata("id");
		if(id!=null && id.trim().length()>0)
		{
			try
			{
				List<WebElement> elements=webDriver.findElements(By.id(id));
				allElements.addAll(elements);
			}
			catch(Exception e)
			{
			}
		}

		//get the name of the widget
		String name=(String)widget.getMetadata("name");
		if(name!=null && name.trim().length()>0)
		{
			try
			{
				List<WebElement> elements=webDriver.findElements(By.name(name));
				allElements.addAll(elements);
			}
			catch(Exception e)
			{
			}
		}

		//if i didn't find any element untill now we try to search with class and tag
		if(allElements.size()==0)
		{
			String className=(String)widget.getMetadata("class");
			if(className!=null && className.trim().length()>0)
			{
				try
				{
					List<WebElement> elements=webDriver.findElements(By.className(className));
					allElements.addAll(elements);
				}
				catch(Exception e)
				{
				}
			}

			if(tag!=null)
			{
				try
				{
					List<WebElement> elements=webDriver.findElements(By.tagName(tag));
					allElements.addAll(elements);
				}
				catch(Exception e)
				{
				}
			}
		}

		//found nothing
		if(allElements.size()==0)
		{
			return null;
		}
		
		WebElement bestElement=findBestMatchingElement(widget, allElements);
		if(bestElement!=null)
		{
			return bestElement;
		}
		
		return null;
	}

	/**
	 * Type a text using Selenium
	 * @param webElement
	 * @param text
	 * @return true if typed
	 */
	private boolean typeSelenium(WebElement webElement, String text)
	{
		if(seleniumKeyTag==null)
		{
			seleniumKeyTag=new String[]{"[ENTER]", "[TAB]", "[DELETE]", "[ESCAPE]", "[BACKSPACE]", "[UP]", "[DOWN]", "[LEFT]", "[RIGHT]", "[PAGE_UP]", "[PAGE_DOWN]", "[HOME]", "[END]", "[F1]", "[F2]", "[F3]", "[F4]", "[F5]", "[F6]", "[F7]", "[F8]", "[F9]", "[F10]", "[F11]", "[F12]", "[NUMPAD_ADD]", "[NUMPAD_SUBTRACT]", "[NUMPAD_MULTIPLY]", "[NUMPAD_DIVIDE]"};
			seleniumKeyValue=new String[]{Keys.RETURN.toString(), Keys.TAB.toString(), Keys.DELETE.toString(), Keys.ESCAPE.toString(), Keys.BACK_SPACE.toString(), Keys.UP.toString(), Keys.DOWN.toString(), Keys.LEFT.toString(), Keys.RIGHT.toString(), Keys.PAGE_UP.toString(), Keys.PAGE_DOWN.toString(), Keys.HOME.toString(), Keys.END.toString(), Keys.F1.toString(), Keys.F2.toString(), Keys.F3.toString(), Keys.F4.toString(), Keys.F5.toString(), Keys.F6.toString(), Keys.F7.toString(), Keys.F8.toString(), Keys.F9.toString(), Keys.F10.toString(), Keys.F11.toString(), Keys.F12.toString(), Keys.ADD.toString(), Keys.SUBTRACT.toString(), Keys.MULTIPLY.toString(), Keys.DIVIDE.toString()};
		}
		try
		{
			StringBuffer buffer=new StringBuffer();
			while(text.length()>0)
			{
				String remainingText=typeSeleniumKeyTag(webElement, text, buffer);
				if(remainingText.length()==text.length())
				{
					// No key tag found
					buffer.append(text.charAt(0));
					text=text.substring(1);
				}
				else
				{
					text=remainingText;
				}
			}
			if(buffer.length()>0)
			{
				webElement.clear();
				webElement.sendKeys(buffer.toString().trim());
			}
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private String typeSeleniumKeyTag(WebElement webElement, String text, StringBuffer buffer)
	{
		for(int i=0; i<seleniumKeyTag.length; i++)
		{
			if(text.startsWith(seleniumKeyTag[i]))
			{
				buffer.append(seleniumKeyValue[i]);
				return text.substring(seleniumKeyTag[i].length());
			}
		}
		return text;
	}

	private void loadCookies()
	{
		String filepath;
		String product=StateController.getProduct();
		if(product.length()>0)
		{
			filepath=DATA_FILEPATH+"/"+product+"/"+COOKIES_FILE;
		}
		else
		{
			filepath=DATA_FILEPATH+"/"+COOKIES_FILE;
		}
		if(actualState!=null && !actualState.isHome())
		{
			filepath+=actualState.getId();
		}
		try
		{
			loadCookies(filepath, webDriver);
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Load cookies from file
	 * @param filepath
	 * @param webDriver
	 */
	private void loadCookies(String filepath, WebDriver webDriver)
	{
		try
		{
			Object object=loadObject(filepath);
			if(object!=null)
			{
				Set<Cookie> cookies=(Set<Cookie>)object;
				for(Cookie cookie:cookies)
				{
					try
					{
						webDriver.manage().addCookie(cookie);
					}
					catch(Exception e)
					{
					}
				}
			}
			webDriver.navigate().refresh();
		}
		catch (Throwable e)
		{
		}
	}

	private void saveCookies()
	{
		String filepath;
		String product=StateController.getProduct();
		if(product.length()>0)
		{
			filepath=DATA_FILEPATH+"/"+product+"/"+COOKIES_FILE;
		}
		else
		{
			filepath=DATA_FILEPATH+"/"+COOKIES_FILE;
		}
		if(actualState!=null && !actualState.isHome())
		{
			filepath+=actualState.getId();
		}
		try
		{
			saveCookies(filepath, webDriver);
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Save cookies to file
	 * @param filepath
	 * @param webDriver
	 * @return
	 */
	private boolean saveCookies(String filepath, WebDriver webDriver)
	{
		try
		{
			Set<Cookie> cookies=webDriver.manage().getCookies();
			return saveObject(filepath, cookies);
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	private Object loadObject(String filepath)
	{
		try
		{
			FileInputStream fileIn = new FileInputStream(filepath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Object object = in.readObject();
			in.close();
			fileIn.close();
			return object;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private boolean saveObject(String filepath, Object object)
	{
		try
		{
			FileOutputStream fileOut = new FileOutputStream(filepath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
			fileOut.getFD().sync();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Convert keyCode and keyChar into text
	 * @param keyChar
	 * @return A text
	 */
	private static String getKeyText(char keyChar)
	{
		if(Character.isAlphabetic(keyChar) || Character.isDigit(keyChar) || Character.isDefined(keyChar))
		{
			return String.valueOf(keyChar);
		}
		return "";
	}

	private void clickWebElement(WebElement element)
	{
		
		String o = element.getAttribute("href");
		System.out.println("Ce so i link: " + o);
		
		if(element.getAttribute("href") != null && ((String) element.getAttribute("href")).length() > 0 ) {
			thisSession.stopPageTiming();
			thisSession.newNode(element.getAttribute("href"));
		}
		
		((JavascriptExecutor)webDriver).executeScript("arguments[0].click();", element);
		
		thisSession.startPageTiming();
	}

	private List<Widget> getWidgets(List<WebElement> elements)
	{
		if(webDriver!=null)
		{
			try
			{
				List<Widget> widgets=new ArrayList<Widget>();

				JavascriptExecutor executor = (JavascriptExecutor) webDriver;
				Object object=executor.executeScript("var result = []; " +
					"function getXPosition(el) {var xPos = 0; while (el) {xPos += (el.offsetLeft - el.scrollLeft + el.clientLeft); el = el.offsetParent;} return xPos;} " +
					"function getYPosition(el) {var yPos = 0; while (el) {yPos += (el.offsetTop - el.scrollTop + el.clientTop); el = el.offsetParent;} return yPos;} " +
					"function getMaxWidth(el) {var children = el.children;var max=el.offsetWidth; for (var i = 0; i < children.length; i++) {var child = children[i]; if (child.offsetWidth>max) max=child.offsetWidth;} return max;} " +
					"function getMaxHeight(el) {var children = el.children;var max=el.offsetHeight; for (var i = 0; i < children.length; i++) {var child = children[i]; if (child.offsetHeight>max) max=child.offsetHeight;} return max;} " +
					"function elementIsVisible(el) {if (getComputedStyle(el).visibility === 'hidden') return false; return true;} " +
					"for (var i=0, max=arguments.length; i < max; i++) { " +
					"   if(elementIsVisible(arguments[i])) result.push({'tag': arguments[i].tagName, 'class': arguments[i].getAttribute('class'), 'type': arguments[i].getAttribute('type'), 'name': arguments[i].getAttribute('name'), 'id': arguments[i].getAttribute('id'), 'value': arguments[i].getAttribute('value'), 'href': arguments[i].getAttribute('href'), 'text': arguments[i].textContent, 'x': getXPosition(arguments[i]), 'y': getYPosition(arguments[i]), 'width': getMaxWidth(arguments[i]), 'height': getMaxHeight(arguments[i]), 'index': i}); " +
					"} " +
					"return JSON.stringify(result); ", elements.toArray());

				String json=object.toString();
				JSONParser parser = new JSONParser();
				JSONArray jsonArray = (JSONArray)parser.parse(json);
				for(int i=0; i<jsonArray.size(); i++)
				{
					JSONObject jsonObject=(JSONObject)jsonArray.get(i);

					String tag=(String)jsonObject.get("tag");
					String className=(String)jsonObject.get("class");
					String type=(String)jsonObject.get("type");
					String name=(String)jsonObject.get("name");
					String id=(String)jsonObject.get("id");
					String value=(String)jsonObject.get("value");
					String href=(String)jsonObject.get("href");
					String text=(String)jsonObject.get("text");
					Long index=(Long)jsonObject.get("index");
					Long x=(Long)jsonObject.get("x");
					Long y=(Long)jsonObject.get("y");
					Long width=(Long)jsonObject.get("width");
					Long height=(Long)jsonObject.get("height");

					if(width>0 && height>0)
					{
						Widget widget=new Widget();
						widget.setLocationArea(new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue()));
						
						widget.putMetadata("tag", tag);
						widget.putMetadata("class", className);
						widget.putMetadata("type", type);
						widget.putMetadata("name", name);
						widget.putMetadata("id", id);
						widget.putMetadata("value", value);
						widget.putMetadata("href", href);
						widget.putMetadata("text", text);
						widget.putMetadata("index", index);

						widgets.add(widget);
					}
				}
				
				return widgets;
			}
			catch (Exception e)
			{
				return null;
			}
		}

		return null;
	}
}
