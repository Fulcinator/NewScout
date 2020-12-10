package plugin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import scout.Action;
import scout.AppState;
import scout.GoHomeAction;
import scout.JSortableButton;
import scout.LeftClickAction;
import scout.NavigateToDialog;
import scout.PluginController;
import scout.StartSessionDialog;
import scout.StateController;
import scout.StateController.Mode;
import scout.StateController.Route;
import scout.Widget;

public class AugmentToolbar
{
	private static Font textFont = new Font("Arial", Font.PLAIN, 12);
	private static Color transparentBlackColor = new Color(0, 0, 0, 160);
	private static Color transparentWhiteColor = new Color(255, 255, 255, 160);
	private static BasicStroke stroke=new BasicStroke(1);
	private static boolean showToolbar=false;
	private static String hoverButtonText=null;
	private static Rectangle menuRect=null;
	private static boolean isBookmarksMenuVisible=false;
	private static boolean isReportsMenuVisible=false;
	private static boolean isPluginsMenuVisible=false;

	public void paintCaptureForeground(Graphics g)
	{
		Graphics2D g2=(Graphics2D)g;

		int marginY=5;
		int toolbarWidth=810;
		int toolbarHeight=50;
		int visibleY=StateController.getVisibleY();
		int visibleWidth=StateController.getVisibleWidth();
		int marginX=(visibleWidth-toolbarWidth)/2;

		if(showToolbar || StateController.isStoppedSession())
		{
			StateController.setToolbarVisible(true);
			
			int x=StateController.getMouseScaledX();
			int y=StateController.getMouseScaledY();
			
			drawRectangle(g2, marginX, visibleY+marginY, toolbarWidth, toolbarHeight, transparentBlackColor, 10);

			hoverButtonText=null;
			
			if(StateController.isStoppedSession())
			{
				drawTextRectangle(g2, "Start", marginX+10, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
			}
			else if(StateController.isRunningSession())
			{
//				drawTextRectangle(g2, "Pause", marginX+10, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
//				drawTextRectangle(g2, "Stop", marginX+10+80, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
				drawTextRectangle(g2, "Stop", marginX+10, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
				if(!StateController.getCurrentState().isHome())
				{
					drawTextRectangle(g2, "Go Home", marginX+85, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
				}
			}
			else if(StateController.isPausedSession())
			{
				drawTextRectangle(g2, "Resume", marginX+10, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
				drawTextRectangle(g2, "Stop", marginX+85, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
			}
			if(StateController.getRoute()==Route.EXPLORING)
			{
				drawTextRectangle(g2, "Explore", marginX+175, visibleY+marginY+10, 70, 30, Color.black, Color.darkGray, Color.white, Color.white);
				drawTextRectangle(g2, "Navigate", marginX+250, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
			}
			else
			{
				drawTextRectangle(g2, "Explore", marginX+175, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
				drawTextRectangle(g2, "Navigate", marginX+250, visibleY+marginY+10, 70, 30, Color.black, Color.darkGray, Color.white, Color.white);
			}
			if(StateController.getMode()==Mode.MANUAL)
			{
				drawTextRectangle(g2, "Manual", marginX+340, visibleY+marginY+10, 70, 30, Color.black, Color.darkGray, Color.white, Color.white);
				drawTextRectangle(g2, "Auto", marginX+415, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
			}
			else
			{
				drawTextRectangle(g2, "Manual", marginX+340, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
				drawTextRectangle(g2, "Auto", marginX+415, visibleY+marginY+10, 70, 30, Color.black, Color.darkGray, Color.white, Color.white);
			}
			drawTextRectangle(g2, "Reset", marginX+505, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
			drawTextRectangle(g2, "Bookmarks", marginX+580, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
			drawTextRectangle(g2, "Reports", marginX+655, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);
			drawTextRectangle(g2, "Plugins", marginX+730, visibleY+marginY+10, 70, 30, Color.white, Color.lightGray, Color.black, Color.black);

			if("Bookmarks".equals(hoverButtonText))
			{
				isBookmarksMenuVisible=true;
			}
			else
			{
				if(isBookmarksMenuVisible && menuRect!=null)
				{
					int xc=StateController.getMouseScaledX();
					int yc=StateController.getMouseScaledY();
					if(!menuRect.contains(xc, yc))
					{
						isBookmarksMenuVisible=false;
					}
				}
			}

			if("Reports".equals(hoverButtonText))
			{
				isReportsMenuVisible=true;
			}
			else
			{
				if(isReportsMenuVisible && menuRect!=null)
				{
					int xc=StateController.getMouseScaledX();
					int yc=StateController.getMouseScaledY();
					if(!menuRect.contains(xc, yc))
					{
						isReportsMenuVisible=false;
					}
				}
			}

			if("Plugins".equals(hoverButtonText))
			{
				isPluginsMenuVisible=true;
			}
			else
			{
				if(isPluginsMenuVisible && menuRect!=null)
				{
					int xc=StateController.getMouseScaledX();
					int yc=StateController.getMouseScaledY();
					if(!menuRect.contains(xc, yc))
					{
						isPluginsMenuVisible=false;
					}
				}
			}

			if(isBookmarksMenuVisible)
			{
				List<String> bookmarks=StateController.getStateTree().getBookmarks();
				int menuItemWidth=200;
				int menuWidth=menuItemWidth+20;
				int menuHeight=15+70+bookmarks.size()*35;
				int menuX=marginX+580+70-menuWidth+10;
				int menuY=visibleY+marginY+20+35;
				menuRect=new Rectangle(menuX, menuY-15, menuWidth, menuHeight+15);
				drawRectangle(g2, menuX, menuY, menuWidth, menuHeight, transparentBlackColor, 10);
				int menuItemY=menuY+10;
				drawTextRectangle(g2, "Store State", menuX+10, menuItemY, menuItemWidth, 30, Color.white, Color.lightGray, Color.black, Color.black);
				menuItemY+=35;
				drawTextRectangle(g2, "Add Bookmark", menuX+10, menuItemY, menuItemWidth, 30, Color.white, Color.lightGray, Color.black, Color.black);
				for(String bookmark:bookmarks)
				{
					menuItemY+=35;
					drawTextRectangle(g2, "Is at "+bookmark, menuX+10, menuItemY, menuItemWidth, 30, Color.white, Color.lightGray, Color.black, Color.black);
				}
			}
			else if(isReportsMenuVisible)
			{
				List<String> reports=PluginController.getReports();
				int menuItemWidth=200;
				int menuWidth=menuItemWidth+20;
				int menuHeight=15+reports.size()*35;
				int menuX=marginX+655+70-menuWidth+10;
				int menuY=visibleY+marginY+20+35;
				menuRect=new Rectangle(menuX, menuY-15, menuWidth, menuHeight+15);
				drawRectangle(g2, menuX, menuY, menuWidth, menuHeight, transparentBlackColor, 10);
				int menuItemY=menuY+10;
				for(String report:reports)
				{
					String name=report.substring(7);
					drawTextRectangle(g2, name, menuX+10, menuItemY, menuItemWidth, 30, Color.white, Color.lightGray, Color.black, Color.black);
					menuItemY+=35;
				}
			}
			else if(isPluginsMenuVisible)
			{
				List<String> plugins=PluginController.getAllClasses();
				int menuItemWidth=200;
				int menuWidth=menuItemWidth+20;
				int menuHeight=15+plugins.size()*35;
				int menuX=marginX+730+70-menuWidth+10;
				int menuY=visibleY+marginY+20+35;
				menuRect=new Rectangle(menuX, menuY-15, menuWidth, menuHeight+15);
				drawRectangle(g2, menuX, menuY, menuWidth, menuHeight, transparentBlackColor, 10);
				int menuItemY=menuY+10;
				for(String plugin:plugins)
				{
					String name=plugin.substring(7);
					boolean enabled=PluginController.isPluginEnabled(plugin);
					if(enabled)
					{
						drawTextRectangle(g2, name, menuX+10, menuItemY, menuItemWidth, 30, Color.black, Color.darkGray, Color.white, Color.white);
					}
					else
					{
						drawTextRectangle(g2, name, menuX+10, menuItemY, menuItemWidth, 30, Color.white, Color.lightGray, Color.black, Color.black);
					}
					menuItemY+=35;
				}
			}
			else
			{
				menuRect=null;
			}

			y=StateController.getMouseScaledY()-visibleY;
			if(y>marginY+50 && menuRect==null)
			{
				showToolbar=false;
			}
		}
		else
		{
			StateController.setToolbarVisible(false);

			marginX=visibleWidth/3;
			drawRectangle(g2, marginX, visibleY+marginY, visibleWidth-marginX*2, 5, transparentWhiteColor, 5);
			
			int y=StateController.getMouseScaledY()-visibleY;
			if(y<5)
			{
				showToolbar=true;
			}
		}
	}

	public void performAction(Action action)
	{
		if(action instanceof LeftClickAction)
		{
			if("Autopilot".equalsIgnoreCase(action.getCreatedByPlugin()))
			{
				// Do not perform auto generated actions
				return;
			}
			if(hoverButtonText!=null)
			{
				// Left mouse click on a button
				if("Start".equals(hoverButtonText) && StateController.isStoppedSession())
				{
					startSession();
				}
				if("Stop".equals(hoverButtonText))
				{
					StateController.setMode(Mode.MANUAL);
/*
					if(!StateController.getCurrentState().isHome())
					{
						// Add a GoHome action
						PluginController.performAction(new GoHomeAction());
					}
*/
					StateController.stopSession();
				}
				if("Go Home".equals(hoverButtonText))
				{
					StateController.setMode(Mode.MANUAL);
					if(StateController.getCurrentState().isHome())
					{
						StateController.displayMessage("Is already at Home");
					}
					else
					{
						GoHomeAction goHomeAction=new GoHomeAction();
						goHomeAction.setCreatedByPlugin("AugmentedToolbar");
						PluginController.performAction(goHomeAction);
					}
				}
				if("Pause".equals(hoverButtonText))
				{
					StateController.setMode(Mode.MANUAL);
					StateController.pauseSession();
				}
				if("Resume".equals(hoverButtonText))
				{
					StateController.resumeSession();
				}
				if("Explore".equals(hoverButtonText) && StateController.getRoute()!=Route.EXPLORING)
				{
					StateController.setRoute(Route.EXPLORING);
				}
				if("Navigate".equals(hoverButtonText) && StateController.getRoute()!=Route.NAVIGATING)
				{
					selectNavigationTarget();
				}
				if("Manual".equals(hoverButtonText) && StateController.getMode()!=Mode.MANUAL)
				{
					StateController.setMode(Mode.MANUAL);
				}
				if("Auto".equals(hoverButtonText) && StateController.getMode()!=Mode.AUTO)
				{
					int percentCovered=StateController.getStateTree().coveredPercent(StateController.getProductVersion());
					if(percentCovered==100)
					{
						StateController.resetCoverage();
					}
					StateController.setMode(Mode.AUTO);
				}
				if("Reset".equals(hoverButtonText))
				{
					StateController.resetCoverage();
				}
				if("Add Bookmark".equals(hoverButtonText))
				{
					String text = JOptionPane.showInputDialog(null, "Enter bookmark name");
					if (text != null && text.length()>0)
					{
						StateController.getCurrentState().setBookmark(text);
					}
				}
				if("Store State".equals(hoverButtonText))
				{
					PluginController.storeHomeState();
				}
				if(hoverButtonText.startsWith("Is at "))
				{
					String bookmarkName=hoverButtonText.substring(6);
					StateController.markBookmark(bookmarkName);
				}
				if(isReportsMenuVisible)
				{
					List<String> reports=PluginController.getReports();
					for(String report:reports)
					{
						String name=report.substring(7);
						if(name.equals(hoverButtonText))
						{
							// Found a report to generate
							PluginController.generateReport(report);
						}
					}
				}
				if(isPluginsMenuVisible)
				{
					List<String> plugins=PluginController.getAllClasses();
					for(String plugin:plugins)
					{
						String name=plugin.substring(7);
						if(name.equals(hoverButtonText))
						{
							// Found a plugin to enable/disable
							boolean enabled=PluginController.isPluginEnabled(plugin);
							PluginController.setPluginEnabled(plugin, !enabled);
						}
					}
				}
			}
		}
	}

	private boolean isOverlapping(List<Widget> widgets, Widget widget)
	{
		for(Widget w:widgets)
		{
			if(StateController.isOverlapping(widget, w))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Draw a rounded rectangle
	 * @param g2
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color
	 */
	private void drawRectangle(Graphics2D g2, int x, int y, int width, int height, Color color, int arc)
	{
		// Draw rectangle
		g2.setColor(color);
		g2.setStroke(stroke);
		g2.fillRoundRect(x, y, width, height, arc, arc);
	}

	private void drawTextRectangle(Graphics2D g2, String text, int x, int y, int width, int height, Color color, Color hoverColor, Color borderColor, Color textColor)
	{
		g2.setFont(textFont);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int centerX=x+width/2;
		int centerY=y+height/2;
		
		FontMetrics fm = g2.getFontMetrics();
		int textWidth = fm.stringWidth(text);
		int textHeight = fm.getHeight();
		int startX=centerX-textWidth/2;
		int startY=centerY-textHeight/2+fm.getMaxAscent();

		int xc=StateController.getMouseScaledX();
		int yc=StateController.getMouseScaledY();
		
		// Draw rectangle
		if(xc>=x && xc<x+width && yc>=y && yc<y+height)
		{
			// Cursor hovers over button
			g2.setColor(hoverColor);
			hoverButtonText=text;
		}
		else
		{
			g2.setColor(color);
		}
		g2.setStroke(stroke);
		g2.fillRoundRect(x, y, width, height, 5, 5);
		g2.setColor(borderColor);
		g2.drawRoundRect(x, y, width, height, 5, 5);

		// Draw text
		g2.setColor(textColor);
		g2.drawString(text, startX, startY);
	}
	
	private void startSession()
	{
		StartSessionDialog dialog = new StartSessionDialog(null);
		if(dialog.showDialog())
		{
			if(dialog.isCanceled())
			{
				return;
			}
			StateController.startSession(dialog.getProduct(), dialog.getProductVersion(), dialog.getTesterName(), dialog.getProductView(), dialog.getHomeLocator(), dialog.getProductWiewWidth(), 
					dialog.getProductWiewHeight(), dialog.isHeadlessBrowser());
		}
	}

	private boolean selectNavigationTarget()
	{
		List<JSortableButton> itemList=new ArrayList<JSortableButton>();
		NavigateToDialog dialog = new NavigateToDialog(null, itemList);

		List<Widget> issues=StateController.getStateTree().getAllIssues();
		ImageIcon issueIcon=new ImageIcon("icons/bug2.png");
		for(Widget issue:issues)
		{
			JSortableButton sortableButton=new JSortableButton(issue.getReportedText(), issue.getId());
			sortableButton.setIcon(issueIcon);
			sortableButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ev)
				{
					String targetWidgetId=sortableButton.getId();
					AppState targetState=StateController.getStateTree().findStateFromWidgetId(targetWidgetId);
					StateController.setNavigationTargetState(targetState);
					StateController.setRoute(Route.NAVIGATING);
					dialog.dispose();
				}
			});
			itemList.add(sortableButton);
		}

		dialog.updateList();
		dialog.showDialog();

		return false;
	}
}
