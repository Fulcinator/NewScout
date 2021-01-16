package plugin;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import scout.Action;
import scout.AppState;
import scout.LeftClickAction;
import scout.MouseScrollAction;
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

public class AppiumPlugin
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
	private static boolean pagesrchaschanged=true;
	private static long startTypeTime=0;

	private static String[] seleniumKeyTag=null;
	private static String[] seleniumKeyValue=null;
	
	private static String old_pageSrc = "";
	
	
	private static String adbPath=System.getenv("LOCALAPPDATA")+"\\Android\\Sdk\\platform-tools";
	private static String emulatorPath=System.getenv("LOCALAPPDATA")+"\\Android\\Sdk\\emulator";
	private static String avdName = "Pixel_2_API_29";
	private static String device_name = "My Phone";
	private static String emulator_name = "emulator-5554";
	private static String platform_name = "Android";
	private static String platform_version = "10.0";
								   
	
	private String package_name = "it.feio.android.omninotes.foss";
	private String activity_name = "it.feio.android.omninotes.MainActivity";
	
	private static AndroidDriver<MobileElement> driver = null;
	
	
	private DesiredCapabilities desiredCapabilities;
	
	//variabili del plugin di gamification:
	private static MobileSession thisSession = null;
	private static ArrayList<String> thisState = null;
	private static boolean isEasterEggAssigned = false;
	
	
	private String printWidget(Widget widget) {
		
		return "Widget: tag = " + widget.getMetadata("tag") + " - class = " + widget.getMetadata("class") + " - type = " + widget.getMetadata("type") + " - name = " + widget.getMetadata("name") + " - id = " + widget.getMetadata("id") + " - value = " + widget.getMetadata("value") + " - href = " + widget.getMetadata("href") + " - text = " + widget.getMetadata("text") + " - index = " + widget.getMetadata("index") + " - rectangle = " + widget.getLocationArea();

		
	}
	
	private String printWidgetNoText(Widget widget) {
		
		return "Widget: tag = " + widget.getMetadata("tag") + " - class = " + widget.getMetadata("class") + " - type = " + widget.getMetadata("type") + " - name = " + widget.getMetadata("name") + " - id = " + widget.getMetadata("id") + " - value = " + widget.getMetadata("value") + " - href = " + widget.getMetadata("href") + " - index = " + widget.getMetadata("index") + " - rectangle = " + widget.getLocationArea();

		
	}
	
	
	//set capabilities for appium execution of the desired app
	private void setDesiredCapabilities() {
		
		desiredCapabilities = new DesiredCapabilities();
		
		//TODO should use system/product properties or fields in the GUI instead of having hardcoded parameters!
		
		desiredCapabilities.setCapability("deviceName", device_name);
		desiredCapabilities.setCapability("udid", emulator_name); 		
		desiredCapabilities.setCapability("platformName", platform_name);
		desiredCapabilities.setCapability("platformVersion", platform_version);
		desiredCapabilities.setCapability("appPackage", package_name);
		desiredCapabilities.setCapability("appActivity", activity_name);
		desiredCapabilities.setCapability("avd", avdName); //uncomment to launch the AVD with appium
		//desiredCapabilities.setCapability("avd", "Galaxy_Nexus_API_25_2"); //uncomment to launch the AVD with appium

		//desiredCapabilities.setCapability("appActivity", "it.feio.android.omninotes.MainActivity");

		desiredCapabilities.setCapability("noReset", "true");					
		desiredCapabilities.setCapability("unicodeKeyboard", true);			//make the keyboard never appear
		desiredCapabilities.setCapability("resetKeyboard", true);
		
		
		//uncomment to obtain screen capture through mjpegscreenshot server
		//https://github.com/dkrivoruchko/ScreenStream
		//desiredCapabilities.setCapability("mjpegScreenshotUrl", "http://127.0.0.1:8080/stream.mjpeg");

		//uncomment to make it headless (NOT WORKING!!)
		//desiredCapabilities.setCapability("avdArgs", "-no-window"); 

	}

	
	//logger for process
	private static void outputProcessLog(Process p) throws IOException {
		
		
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) { break; }
			if (line == "") { logStuff("no output!"); break; }
			//System.out.println(line);
		}

		
	}
	
	//logger
	private static void logStuff(String stringtolog)  {
		
		try {
			
			BufferedWriter wr = new BufferedWriter(new FileWriter("log.txt",true));

			wr.write(stringtolog + "\n");
			wr.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		
	}
	
	public static void adbKillServer() throws IOException {
		ProcessBuilder builder = new ProcessBuilder(
				"cmd.exe", "/c\"", adbPath + "\\adb\" kill-server");

		builder.redirectErrorStream(true);
		Process p = builder.start();
		
		outputProcessLog(p);
	}

	//start adb server
	//TODO: add check if adb is already started do not start it
	private void adbStartServer() throws IOException, InterruptedException {

		
		ProcessBuilder builder = new ProcessBuilder(
				"cmd.exe", "/c", "adb start-server");

		
		builder.redirectErrorStream(true);
		
		builder.directory(new File(adbPath));
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;

		outputProcessLog(p);
		//TODO check that now it is launched!

		
		logStuff("adb server launched");
	}

	
		
	//get an instance of the appium driver
	private AndroidDriver<MobileElement> getAppiumDriver() throws InterruptedException, IOException {
		
		//start appium server
		
		Runtime runtime = Runtime.getRuntime();

		runtime.exec("cmd.exe /c start cmd.exe /k \"appium -a 0.0.0.0 -p 4723 -dc \"{\"\"noReset\"\": \"\"false\"\"}\"\"");

		//attach to current activity and start driver

		List<String> param = GamificationUtils.loadStats("AppiumConf");
		
		if(param.size() >= 7) {
			avdName = param.get(1);
			emulator_name = param.get(2);
			platform_name = param.get(3);
			platform_version = param.get(4);
			package_name = param.get(5);
			activity_name = param.get(6);
		}
		
		logStuff("server launched");
		Thread.sleep(Integer.parseInt(param.get(0)));
		
		setDesiredCapabilities();
		AndroidDriver<MobileElement> driver = new AndroidDriver<MobileElement>(new URL("http://0.0.0.0:4723/wd/hub"), desiredCapabilities);
		
		logStuff("connection to app ok");

		driver.context("NATIVE_APP");
		logStuff("context set");


		return driver;
		
	}

	//get capture of current screen through adb specific function
		private BufferedImage getScreenshotAdb(String screenshot_name) {
			
			long getscreenshotadb_before = System.currentTimeMillis();

			BufferedImage in = null;
			
			try {
			
			ProcessBuilder builder = new ProcessBuilder(
					"cmd.exe", "/c\"", adbPath + "\\adb\" shell screencap -p /sdcard/" + screenshot_name + ".png");
			 Process process = builder.start();
			process.waitFor();
			//Thread.sleep(1000);

			builder = new ProcessBuilder(
					"cmd.exe", "/c\"", adbPath + "\\adb\" pull /sdcard/" + screenshot_name + ".png");
			 process = builder.start();
			process.waitFor();
		//	Thread.sleep(1000);

			builder = new ProcessBuilder(
					"cmd.exe", "/c\"", adbPath + "\\adb\" shell rm /sdcard/" + screenshot_name + ".png");
			 process = builder.start();
			process.waitFor();

		//	Thread.sleep(1000);

			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
			File img = new File(screenshot_name + ".png");
			 in = ImageIO.read(img);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			
			//TODO ridimensionamento dell'immagine
			
			long getscreenshotavd_after = System.currentTimeMillis();

			
			long time_getscreenshotadb = getscreenshotavd_after - getscreenshotadb_before;
			
			System.out.println("time to get screenshot with adb = " + time_getscreenshotadb);

			return in;
		}
	
	
	
		public static BufferedImage resizeScreenshot(BufferedImage src, int original_screen_width, int new_screen_width) {
			
			
			int current_img_width = src.getWidth();
			int current_img_height = src.getHeight();
			
			float ratio = new_screen_width / (float) original_screen_width;
			
			int new_width = (int) (current_img_width * ratio);
			int new_height = (int) (current_img_height * ratio);
			
			BufferedImage outputImage = new BufferedImage(new_width, new_height, src.getType());
			
			ResampleOp resizeOp = new ResampleOp(new_width, new_height);
			resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
			BufferedImage scaledImage = resizeOp.filter(src, null);

			// scales the input image to the output image
			Graphics2D g2d = outputImage.createGraphics();
			g2d.drawImage(src, 0, 0, new_width, new_height, null);
			g2d.dispose();
			
			return scaledImage;

			
		}
	
		
		
		
	//gets capture of current screen with Appium
	//http://appium.io/docs/en/commands/session/screenshot/
	private BufferedImage getScreenshotWithAppium() throws WebDriverException, IOException {
		
		long getscreenshotappium_before = System.currentTimeMillis();

		File scrFile = driver.getScreenshotAs(OutputType.FILE);


		
        InputStream ins = new FileInputStream(scrFile);

        OutputStream outs = new FileOutputStream(new File("output_screen.png"));

        byte[] buf = new byte[1024];
        int len;
        while ((len = ins.read(buf)) > 0) {
            outs.write(buf, 0, len);
        }
        ins.close();
        outs.close();

		//BufferedImage in2 = ImageIO.read(new ByteArrayInputStream(driver.getScreenshotAs(OutputType.BYTES)));
		//BufferedImage in3 = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(driver.getScreenshotAs(OutputType.BASE64))));
		BufferedImage in = null;

		try {
		 in = ImageIO.read(scrFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		long getscreenshotappium_after = System.currentTimeMillis();
		long time_getscreenshotappium = getscreenshotappium_after - getscreenshotappium_before;
		
		
		
		
		//System.out.println("height = " + in.getHeight() + " - width = " + in.getWidth());
		
		
		//to resize the image
		/*
		
		BufferedImage before = in;
		int w = before.getWidth();
	    int h = before.getHeight();
	    // Create a new image of the proper size
	    int scale = 2;
	    int w2 = (int) (w * scale);
	    int h2 = (int) (h * scale);
	    BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
	    AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
	    AffineTransformOp scaleOp 
	        = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);

	    Graphics2D g2 = (Graphics2D) after.getGraphics();
	    // Here, you may draw anything you want into the new image, but we're
	    // drawing a scaled version of the original image.
	    g2.drawImage(before, scaleOp, 0, 0);
	    g2.dispose();		

		return  after; */
		//TODO: eventually uncomment this
		//System.out.println("time for get screenshot appium = " + time_getscreenshotappium);

		return in;
		
		//return resizeScreenshot(in, 1080, 540);
	}
	
	
	public void startSession() throws IOException, InterruptedException
	{
		try {
		
			//EXPLICITLY CALL FUNCTIONS IF ADB AND AVD ARE NOT EXTERNALLY LAUNCHED (FROM SHELL COMMANDS OR ANDROIDSTUDIO)
		//adbStartServer();
		//adbStartAvd();
		logStuff("avd started");
		//TODO sleep when the process is finished
		Thread.sleep(4000);

		
		/*AppiumDriverLocalService service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
	            .withArgument(new ServerArgument(){
	                public String getArgument() {
	                    return "--avd";
	                }
	            }, "Nexus_5X_API_25")
	            .usingDriverExecutable(new File("/Applications/Appium.app/Contents/Resources/node/bin/node"))
	            .withAppiumJS(new File("/Applications/Appium.app/Contents/Resources/node_modules/appium/bin/appium.js"))
	            .withLogFile(new File("target/\"+deviceUnderExecution+\".log")));

	    service.start();*/

		driver = getAppiumDriver();
		

		//set native context for the driver
		Thread.sleep(3000);

		}
		catch (Exception e) {
			e.printStackTrace();
			logStuff("Exception " + e.getMessage());
		}
		
		
		if (driver != null) {
			
			StateController.setSessionState(SessionState.RUNNING);
			actualState=StateController.getCurrentState();
			if(actualState.getMetadata("cookies")!=null)
			{
				loadCookies();
			}
			
			//TODO: GAMIFICATION caricare lo iniziale dell'app
			String mainActivity = package_name + "/" +activity_name;
			thisState = getAppState(mainActivity);
			thisSession = new MobileSession(mainActivity ,thisState, StateController.getTesterName(), true);
			thisSession.startSessionTiming();
		}
		
		
		
	}

	
	
	public void stopSession()
	{
		thisSession.stopSessionTiming();
		thisSession.computeTimeSession();
		
		if(driver!=null)
		{
			
			try {
				//Runtime.getRuntime().exec("adb -s emulator-5554 emu kill");
				Runtime.getRuntime().exec("adb shell pm clear " + activity_name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			driver.quit();
		}
		
		System.out.println(thisSession.getStringTiming());
		thisSession.getRoot().printTiming();
		//GamificationUtils.writeSession(thisSession);
	}

	public void storeHomeState()
	{
		saveCookies();
		if(actualState!=null)
		{
			actualState.putMetadata("cookies", true);
		}
	}

	
	//TODO: need to be adapted to appium?
	public String[] getProductViews()
	{
		return new String[] {"Chrome", "Firefox"};
	}
	
	
	public BufferedImage getCapture() throws WebDriverException, IOException
	{
		if(StateController.isOngoingSession())
		{
			if (pagesrchaschanged) {
				
				BufferedImage image = null;
				
				image = getScreenshotWithAppium();
			if(image!=null)
			{
				// Update the status of all widgets
				//long time_before_verifyandreplace = System.currentTimeMillis();
				verifyAndReplaceWidgets();
				//long time_after_verifyandreplace = System.currentTimeMillis();
				//System.out.println("time to verifyandreplace = " + (time_after_verifyandreplace - time_before_verifyandreplace));

			}
			return image;
			}
			else {
				System.out.println("no screen update - not updating");
				return null;
			}
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
	
	//execute a click action on the center of a widget just by x and y coordinates (not using appium)
	//to be used when a unique widget cannot be identified through id, text or content description
	public void performAdbClick(Action action) {
		
		
		if (action instanceof LeftClickAction) {

			
			LeftClickAction leftClickAction=(LeftClickAction) action;

			//Widget locatedWidget=StateController.getWidgetAt(actualState, leftClickAction.getLocation());

			int x = (int) leftClickAction.getLocation().getX();
			int y = (int) leftClickAction.getLocation().getY();
			
			
			System.out.println("trying to launch /c\"" + adbPath + "\\adb\" shell input mouse tap " + x + " " + y );

			//adb shell input mouse tap 523 126
			ProcessBuilder builder = new ProcessBuilder(
					"cmd.exe", "/c\"", adbPath + "\\adb\" shell input mouse tap " + x + " " + y);

			try {
				Process p = builder.start();
				System.out.println("Tap performed at x=" + x + ", y= " + y + "con codice " + p.waitFor());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(InterruptedException e) {
				System.err.println("Errore nella wait del processo:" + e.getMessage() );
				System.err.println(e.getStackTrace());
			}
		}
	}

	
	
	
	public void performAction(Action action)
	{
		
		if(!StateController.isRunningSession() || (StateController.isToolbarVisible() && StateController.getMode()==Mode.MANUAL && !"AugmentedToolbar".equalsIgnoreCase(action.getCreatedByPlugin())))
		{
			// Only perform actions during a running session and not showing toolbar
			return;
		}

		System.out.println(">STARTING PERFORMACTION");
		
		long time_performaction_before = System.currentTimeMillis();
	
		if(action instanceof MouseScrollAction)
		{
			MouseScrollAction mouseScrollAction=(MouseScrollAction)action;
			StateController.setSelectedWidgetNo(StateController.getSelectedWidgetNo()+mouseScrollAction.getRotation());
			return;
		}
		
		else if(action instanceof TypeAction)
		{
			
			//type action should be the same because they are managed all by the GUI
			
			TypeAction typeAction=(TypeAction)action;
			KeyEvent keyEvent=typeAction.getKeyEvent();
			int keyCode = keyEvent.getKeyCode();
			char keyChar = keyEvent.getKeyChar();

			if(keyCode==KeyEvent.VK_ENTER)
			{
				// Send keyboardInput to selected widget (if any)
				StateController.addKeyboardInput("[ENTER]");
				
				Widget locatedWidget=StateController.getWidgetAt(actualState, typeAction.getLocation());
				//GAMIFICATION: record interaction in the page
				thisSession.newInteraction(GamificationUtils.logInformationAndroid(locatedWidget));
				
				performTypeActionOnWidgetAppium(locatedWidget, action);
				
				
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
			
			System.out.println("exiting perform action");
			long time_performaction_after = System.currentTimeMillis();

			long time_performaction = time_performaction_after - time_performaction_before;
			System.out.println("time to performaction = " + time_performaction);

		}
		
		
		
		if (action instanceof LeftClickAction) {		
			
			
			//left mouse click on a widget
			LeftClickAction leftClickAction=(LeftClickAction) action;
			System.out.println("this is a left click at" + leftClickAction.getLocation());
			
			//temporary mgmt of back, menu and home buttons
			//back button management should be managed by adding the back button to the available buttons in the getavailablewidgets

			if (leftClickAction.getLocation().x > 200 && leftClickAction.getLocation().x < 300 &&
					leftClickAction.getLocation().y > 1800 && leftClickAction.getLocation().x < 1900 ) {
				
					driver.pressKeyCode(AndroidKeyCode.BACK);
					return;

			}
			
			
			else if (leftClickAction.getLocation().x > 490 && leftClickAction.getLocation().x < 590 &&
					leftClickAction.getLocation().y > 1800 && leftClickAction.getLocation().x < 1900 ) {
				
					driver.pressKeyCode(AndroidKeyCode.HOME);
					
			}
			
			
			else if (leftClickAction.getLocation().x > 780 && leftClickAction.getLocation().x < 880 &&
					leftClickAction.getLocation().y > 1800 && leftClickAction.getLocation().x < 1900 ) {
				
					driver.pressKeyCode(AndroidKeyCode.MENU);
					
			}

			Widget locatedWidget=StateController.getWidgetAt(actualState, leftClickAction.getLocation());
			
			
			if (locatedWidget == null) {
				
				System.out.println("located widget is null!");
			}
			
			if(locatedWidget!=null)
			{
				
				
				if(locatedWidget.getWidgetType()==WidgetType.ACTION)
				{
					
					
					if (locatedWidget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION) {
						thisSession.newInteraction(GamificationUtils.logInformationAndroid(locatedWidget));
						performTypeActionOnWidgetAppium(locatedWidget, leftClickAction);
						//GAMIFICATION: type action, debug and try to record interaction
					}
					
					else if(locatedWidget.getWidgetSubtype()==WidgetSubtype.LEFT_CLICK_ACTION) {
						//TODO: debug these case, try to debug
						
						AppState prima = StateController.getCurrentState();
						MobilePage p = (MobilePage) thisSession.getCurrent().getPage();
						p.setScoutState(prima);
						
						if (locatedWidget.getMetadata("id") != null && !locatedWidget.getMetadata("id").toString().equals("")) {	
							try { driver.findElementById(locatedWidget.getMetadata("id").toString()).click(); }
							catch (Exception e) { e.printStackTrace();	}
							

							
						}
						
						else if (locatedWidget.getMetadata("text") != null && !locatedWidget.getMetadata("text").toString().equals("")) {
							try { driver.findElement(By.xpath("//*[@text='" + locatedWidget.getMetadata("text") + "']")).click(); }
							catch (Exception e) { e.printStackTrace();	}
						}
						
						else if (locatedWidget.getMetadata("content-desc") != null && !locatedWidget.getMetadata("content-desc").toString().equals("")) {
							try { driver.findElement(By.xpath("//*[@content-desc='" + locatedWidget.getMetadata("content-desc") + "']")).click(); }
			
							catch (Exception e) { e.printStackTrace(); }
						}
						
						else { 
							System.out.println("done by click");
							performAdbClick(action);
						}
						
						thisSession.newInteraction(GamificationUtils.logInformationAndroid(locatedWidget));
						
						//TODO STATE MGMT NOT FULLY DONE BY NOW
						if(locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN || locatedWidget.getWidgetVisibility()==WidgetVisibility.SUGGESTION)
						{
							locatedWidget.setWidgetVisibility(WidgetVisibility.VISIBLE);
							locatedWidget.setComment(null);
							StateController.insertWidget(locatedWidget, locatedWidget.getNextState());
						}
						else
						{
							StateController.performWidget(locatedWidget);
						}
						
						boolean isEEAssignable = false;
						//GAMIFICATION: controllo se ho cliccato sull'easter egg
						if (locatedWidget.getMetadata("id") != null) {
							if(thisSession.getCurrent().getPage().getSonWithEasterEgg().equals(locatedWidget.getMetadata("id"))) {
								isEEAssignable = true;
							}
						}
						
						//GAMIFICATION: verifica del fragment:
						ArrayList<String> state = getAppState(thisSession.getMainActivity());
						plugin.Node current = thisSession.getCurrent(); 
						plugin.Node n = thisSession.updateState(state);
						if(n != null) { 
							if(n.equals(current)) {//non era presente, quindi aggiungo il figlio
								p = (MobilePage) thisSession.newNode(state).getPage();
								p.setScoutState(StateController.getCurrentState());
								if(thisSession.getCurrent().getPage().getEasterEggStartPoint() == null && isEEAssignable) {
									thisSession.getCurrent().getPage().setHasEasterEgg(true);
									int width=StateController.getProductViewWidth();
								  	int height=StateController.getProductViewHeight();
								  	int x =  (int)(Math.random() * (width - 30));
								  	int y =  (int)(Math.random() * (height - 50));
								  	thisSession.getCurrent().getFather().getPage().setEasterEggStartPoint(x, y);
								}
								
								System.out.println("Ho creato in nuovo nodo con stato: " + p.getState());
								
							} else {
								MobilePage mp = (MobilePage) n.getPage();
								if(mp != null)
									StateController.setCurrentState(mp.getScoutState());
								
								System.out.println("Ho ripescato il nodo con stato: " + p.getState());
							}
							thisState = state;
						} else {
							StateController.setCurrentState(prima);
							System.out.println("Lo stato è rimasto lo stesso: " + p.getState());
						}
						
						//thisState = state;
						
					}
					
					

				}
				
				else if(locatedWidget.getWidgetType()==WidgetType.CHECK) {
					
					
					if (StateController.getKeyboardInput().length() > 0 && locatedWidget.getWidgetVisibility()==WidgetVisibility.VISIBLE) {
						
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
						
					else
					{
						if(locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN)
						{
							locatedWidget.setWidgetVisibility(WidgetVisibility.VISIBLE);
							locatedWidget.setCreatedBy(StateController.getTesterName());
							locatedWidget.setCreatedDate(new Date());
							locatedWidget.setCreatedProductVersion(StateController.getProductVersion());
						}
					}
	
					//GAMIFICATION: record interaction in this page
					thisSession.newInteraction(GamificationUtils.logInformationAndroid(locatedWidget));
				}
				
				
				else if(locatedWidget.getWidgetType()==WidgetType.ISSUE)
				{
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
						
						//GAMIFICATION: record interaction in this page
						thisSession.newInteraction(GamificationUtils.logInformationAndroid(locatedWidget));
					}
				}


			}
						

			long time_performaction_after = System.currentTimeMillis();

			long time_performaction = time_performaction_after - time_performaction_before;
			System.out.println("time to performaction = " + time_performaction);
			System.out.println("ENDING PERFORMACTION");

		}
		

	}

	
	
	
	
	private void performTypeActionOnWidgetAppium(Widget locatedWidget, Action action) {
		
		
		
		String text = StateController.getKeyboardInput();
		
		
		System.out.println("received to type: " + text);
		if(seleniumKeyTag==null)
		{
			seleniumKeyTag=new String[]{"[ENTER]", "[TAB]", "[DELETE]", "[ESCAPE]", "[BACKSPACE]", "[UP]", "[DOWN]", "[LEFT]", "[RIGHT]", "[PAGE_UP]", "[PAGE_DOWN]", "[HOME]", "[END]", "[F1]", "[F2]", "[F3]", "[F4]", "[F5]", "[F6]", "[F7]", "[F8]", "[F9]", "[F10]", "[F11]", "[F12]", "[NUMPAD_ADD]", "[NUMPAD_SUBTRACT]", "[NUMPAD_MULTIPLY]", "[NUMPAD_DIVIDE]"};
			seleniumKeyValue=new String[]{Keys.RETURN.toString(), Keys.TAB.toString(), Keys.DELETE.toString(), Keys.ESCAPE.toString(), Keys.BACK_SPACE.toString(), Keys.UP.toString(), Keys.DOWN.toString(), Keys.LEFT.toString(), Keys.RIGHT.toString(), Keys.PAGE_UP.toString(), Keys.PAGE_DOWN.toString(), Keys.HOME.toString(), Keys.END.toString(), Keys.F1.toString(), Keys.F2.toString(), Keys.F3.toString(), Keys.F4.toString(), Keys.F5.toString(), Keys.F6.toString(), Keys.F7.toString(), Keys.F8.toString(), Keys.F9.toString(), Keys.F10.toString(), Keys.F11.toString(), Keys.F12.toString(), Keys.ADD.toString(), Keys.SUBTRACT.toString(), Keys.MULTIPLY.toString(), Keys.DIVIDE.toString()};
		}

		if (locatedWidget.getWidgetType() == WidgetType.ACTION && locatedWidget.getWidgetSubtype() == WidgetSubtype.TYPE_ACTION) {
			
			try
			{
				
				String stringtotype = text.split("\\[ENTER\\]")[0];
				
				
				if(stringtotype.length()>0)
					{
					
					
					System.out.println("we have keyboard input to type");
						// We have keyboard input to type
						MobileElement element;
						if (locatedWidget.getMetadata("id") != null && !locatedWidget.getMetadata("id").toString().equals("")) {	
							try { 
								
								element = driver.findElementById(locatedWidget.getMetadata("id").toString());
								if (element != null) { System.out.println("found element"); } 
								element.sendKeys(stringtotype);
								}
							catch (Exception e) { e.printStackTrace();	}
						}
						
						else if (locatedWidget.getMetadata("text") != null && !locatedWidget.getMetadata("text").toString().equals("")) {
							try { 
								System.out.println("found by text");
								element = driver.findElement(By.xpath("//*[@text='" + locatedWidget.getMetadata("text") + "']"));
								element.clear();
								element.sendKeys(stringtotype); 
							}
							catch (Exception e) { e.printStackTrace();	}
						}
						
						else if (locatedWidget.getMetadata("content-desc") != null && !locatedWidget.getMetadata("content-desc").toString().equals("")) {
							try { 
								System.out.println("found by contdesc");
								element = driver.findElement(By.xpath("//*[@content-desc='" + locatedWidget.getMetadata("content-desc") + "']"));
								element.clear();
								element.sendKeys(stringtotype); 
								}
			
							catch (Exception e) { e.printStackTrace(); }
						}
					}
					else
					{
						
						System.out.println("left click on a widget");
			
						System.out.println("must execute a click on " + printWidgetNoText(locatedWidget));
						
						if (locatedWidget.getMetadata("id") != null && !locatedWidget.getMetadata("id").toString().equals("")) {	
							try { driver.findElementById(locatedWidget.getMetadata("id").toString()).click(); }
							catch (Exception e) { e.printStackTrace();	}
						}
						
						else if (locatedWidget.getMetadata("text") != null && !locatedWidget.getMetadata("text").toString().equals("")) {
							try { driver.findElement(By.xpath("//*[@text='" + locatedWidget.getMetadata("text") + "']")).click(); }
							catch (Exception e) { e.printStackTrace();	}
						}
						
						else if (locatedWidget.getMetadata("content-desc") != null && !locatedWidget.getMetadata("content-desc").toString().equals("")) {
							try { driver.findElement(By.xpath("//*[@content-desc='" + locatedWidget.getMetadata("content-desc") + "']")).click(); }
							catch (Exception e) { e.printStackTrace(); }
						}
						else { 
							performAdbClick(action);
						}
	
				
				
					}
			
			StateController.clearKeyboardInput();
			
			}
			
			
			
			
			
			
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		else if (locatedWidget.getWidgetType() == WidgetType.CHECK) {
			
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
		//System.out.println("exiting performtype");
		
	}
	
	//TODO
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
	 * Verify that visible widgets are still valid and replace hidden widgets
	 */
	private synchronized void verifyAndReplaceWidgets()
	{
		
		
		//System.out.println("we're inside verify and replace widgets");
		try
		{
			List<Widget> availableWidgets=getAvailableWidgetsAppium();
			if(availableWidgets==null)
			{
				return;
			}
			List<Widget> hiddenAvailableWidgets=new ArrayList<Widget>();
			List<Widget> nonHiddenWidgets=actualState.getNonHiddenWidgets();
			hiddenAvailableWidgets.addAll(availableWidgets);
			/*for(Widget widget:availableWidgets)
			{
				hiddenAvailableWidgets.add(widget);
			}*/
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
					//System.out.println("we're here;");
					if (widgetMatch==null) {
						//System.out.println("widgetmatch for widget " + widget.getMetadata("id") + " is null");
					}
					if(widgetMatch!=null)
					{
						Widget matchingWidget=widgetMatch.getWidget2();

						widget.putMetadata("matching_widget", matchingWidget);

						if(widget.getValidExpression()!=null)
						{
							if(matchingWidget.evaluateExpression(widget.getValidExpression()))
							{
								//System.out.println("widget " + widget.getMetadata("id") + " is valid");
								// Expression is valid
								widget.setWidgetStatus(WidgetStatus.VALID);
								widget.setComment(widget.getValidExpression());
							}
							else
							{
								// Expression not valid
								//System.out.println("widget " + widget.getMetadata("id") + " is located");

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
							//System.out.println("widget " + widget.getMetadata("id") + " is located");

							widget.setWidgetStatus(WidgetStatus.LOCATED);
						}
						hiddenAvailableWidgets.remove(widget);
					}
					else
					{
						// Could not locate widget
						//System.out.println("widget " + widget.getMetadata("id") + " is unlocated");

						widget.setWidgetStatus(WidgetStatus.UNLOCATED);
					}
				}
			}
			
			//TODO: filter list is possibol? eventuale generazione easter egg -- 790 selenium plugin

			actualState.replaceHiddenWidgets(hiddenAvailableWidgets, "SeleniumPlugin");
			
			//questo filtro potrebbe non servire poiché è difficile che esistano widget così piccoli
			List<Widget> l = hiddenAvailableWidgets.stream()
		  			.filter(w -> (w.getLocationArea().height > 1 && w.getLocationArea().width > 1))
		  			.collect(Collectors.toList());
			
			if(thisSession != null) {
				//questo serve per i bug noti
		  		/*Long x = l.stream()
		  		.filter(w -> w.getMetadata("text") != null)
		  		.map(w -> w.getMetadata("text"))
		  		.filter(text -> knownBug.contains(text))
		  		.count();*/
		  		
		  		
			  	if(!isEasterEggAssigned && thisSession.getCurrent().getPage().getSonWithEasterEgg() == null) {
			  		//l'easter egg è assegnato ad un widget cliccabile e con un id non nullo
			  		List<String> eggable = l.stream()
			  				/*.filter( w -> isClickableWidgetAppium(w))
			  				.map( w -> w.getMetadata("id").toString())
			  				.collect(Collectors.toList());*/
			  				.filter(w -> w.getWidgetType() == WidgetType.ACTION)
			  				.filter(w -> w.getWidgetSubtype() == WidgetSubtype.LEFT_CLICK_ACTION && w.getMetadata("id") != null)
			  				.map( w -> w.getMetadata("id").toString())
			  				.collect(Collectors.toList());
			  				//tolto perché serviva per riconoscere il javascript
			  				//.filter( o -> !(o.contains("#") || o.contains("javascript:") || o.contains("mailto:") || o.contains("tel:") || o.contains("ftp://") ||  o.length() <= 0) || (o.contains("#") && o.contains("?")))
			  				
			  		
			  		/*if(x != 0) {
			  			thisSession.setBugCount(thisSession.getBugCount() + 1);
			  		}*/
			  		
			  		if(eggable.size() > 0) {
					  	int max = eggable.size() -1;
					  	int index = (int)(Math.random() * max);
					  	thisSession.getCurrent().getPage().setSonWithEasterEgg(eggable.get(index));
					  	//leadToEasterEgg = eggable.get(index);
					  	System.out.println("The easter egg is " + eggable.get(index));
					  	isEasterEggAssigned = true;
			  		}
			  	}
			  	
				//System.out.println("La lista filtrata ha " + l.size() + " elementi");
				//System.out.println("La lista non filtrata ha " + hiddenAvailableWidgets.size() + " elementi");
			  	
				thisSession.setActiveWidgetCurrentPage(nonHiddenWidgets.size());
				//we use only the widgets that fits in the page
				thisSession.setTotalWidgetCurrentPage(l.size());
		  	}
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
			WidgetMatch widgetMatch=matchScoreAppium(widget, availableWidget);
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


	private boolean isClickableWidgetAppium(Widget widget) {
		//in this case it is possible to use the android layout "clickable" property
		String clickable=(String)widget.getMetadata("clickable");
		if (clickable.equals("true")) return true;
		else return false;
		
	}
	
	private boolean isTypeWidgetAppium(Widget widget) {
		
		String widgetclass = (String) widget.getMetadata("type");
		if (widgetclass.equals("android.widget.EditText")) { return true; }
		
		return false;
	}
	
	private boolean isLongClickableWidgetAppium (Widget widget) {
		
		String longclickable = (String) widget.getMetadata("long-clickable");
		if (longclickable.equals("true")) return true;
		else return false;
	}
	
	private boolean isCheckWidgetAppium (Widget widget) {
		
		String text = (String) widget.getMetadata("text");
		if (text.trim().length() > 0) { 
			//System.out.println("text is " + text);
			return true; }
		
		String widgetclass = (String) widget.getMetadata("type");
		if (widgetclass.contains("android.widget.TextView")) { 
			//System.out.println("widgetclass is " + text);
			return true; }
		
		return false;
	}


	

	/**
	 * Calculate a weighted match percent
	 * @param widget1
	 * @param widget2
	 * @return A match or null
	 */
	
	
	
	private WidgetMatch matchScoreAppium(Widget widget1, Widget widget2) {
		
		
		
		String id1=(String)widget1.getMetadata("id");
		String type1=(String)widget1.getMetadata("type");
		String text1=(String)widget1.getMetadata("text");
		String contdesc1=(String)widget1.getMetadata("content-desc");
		Rectangle locationArea1=widget1.getLocationArea();
		
		String id2=(String)widget2.getMetadata("id");
		String type2=(String)widget2.getMetadata("type");
		String text2=(String)widget2.getMetadata("text");
		String contdesc2=(String)widget2.getMetadata("content-desc");
		Rectangle locationArea2=widget2.getLocationArea();
		
		if (type1==null || type2==null || !type1.equalsIgnoreCase(type2)) {
			return null;
			//different class
		}
		
		int score = 0;
		int totalScore = 0;
		
		if(bothContainsValue(text1, text2))
		{
			if(text1.length()>5)
			{
				// Long text
				totalScore+=100;
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
				totalScore+=50;
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

		
		if(bothContainsValue(id1, id2))
		{
			totalScore+=100;
			if(id1.equals(id2))
			{
				score+=100;
			}
		}
		
		if(bothContainsValue(contdesc1, contdesc2))
		{
			totalScore+=50;
			if(id1.equals(id2))
			{
				score+=50;
			}
		}
		
		if(bothContainsValue(type1, type2))
		{
			totalScore+=20;
			if(type1.equals(type2))
			{
				score+=20;
			}
		}
		if(locationArea1!=null && locationArea2!=null)
		{
			totalScore+=20;
			if(Math.abs(locationArea1.getX()-locationArea2.getX())<5 && Math.abs(locationArea1.getY()-locationArea2.getY())<5)
			{
				score+=20;
			}
			else if(Math.abs(locationArea1.getX()-locationArea2.getX())<5 || Math.abs(locationArea1.getY()-locationArea2.getY())<5)
			{
				score+=10;
			}
			totalScore+=10;
			if(Math.abs(locationArea1.getWidth()-locationArea2.getWidth())<5 && Math.abs(locationArea1.getHeight()-locationArea2.getHeight())<5)
			{
				score+=20;
			}
			else if(Math.abs(locationArea1.getWidth()-locationArea2.getWidth())<5 || Math.abs(locationArea1.getHeight()-locationArea2.getHeight())<5)
			{
				score+=10;
			}
		}

		if(totalScore==0)
		{
			return null;
		}
		
		return new WidgetMatch(widget1, widget2, score);
		
		
	}
	
	//RC: utility function
	private boolean bothContainsValue(String value1, String value2)
	{
		if(value1!=null && value2!=null && value1.trim().length()>0 && value2.trim().length()>0)
		{
			return true;
		}
		return false;
	}
	
		
	
	
	private List<Widget> getAvailableWidgetsAppium()  {
		
		ArrayList<Widget> widgetList = new ArrayList<Widget>();


				String pageSrc = driver.getPageSource();	
				
				
				if (pageSrc == old_pageSrc) {
					pagesrchaschanged = false;
				}
				else {
					pagesrchaschanged = true;
					old_pageSrc = pageSrc;
				}
				
				
				
				
				
				//System.out.println(pageSrc);
				
				
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = null;
				try {
					builder = factory.newDocumentBuilder();
				} catch (ParserConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				Document document = null;
				try {
					document = builder.parse(new InputSource(new StringReader(pageSrc)));
				} catch (SAXException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				document.getDocumentElement().normalize();

				//System.out.println(document.toString());
				//TODO : PUT TIMERS
				//System.out.println("Root element: " + document.getDocumentElement().getNodeName());
				NodeList nodeList = document.getElementsByTagName("*");
				
				long timebefore_pgsrc_analysis = System.currentTimeMillis();
				//System.out.println("there are " + nodeList.getLength() + " elements");
				for (int temp = 0; temp < nodeList.getLength(); temp++) {
					
					
					Boolean no_type = true;
					
				    org.w3c.dom.Node node = nodeList.item(temp);
				    
					if (node.getNodeName().equals("hierarchy")) continue;

				   // System.out.println("\nCurrent element: " + node.getNodeName() + " - " + node.getAttributes().getNamedItem("class") + node.getAttributes().getNamedItem("resource-id"));
				    
				    Widget curr_widget = new Widget();
				    
				    
				    

				    curr_widget.setCreatedBy("tester1"); // to be filled in the real app
				    curr_widget.setCreatedDate(new Date()); // to be filled in the real app
				    curr_widget.setCreatedProductVersion("version1"); // to be filled in the real app

				    String class_name = node.getAttributes().getNamedItem("class").getNodeValue();
				    String position = node.getAttributes().getNamedItem("bounds").getNodeValue();
				    
				    
				    

				    int upperleft_x = Integer.valueOf(  ((position.split("\\["))[1].split(","))[0]);
				    int upperleft_y = Integer.valueOf(((position.split("\\[")[1]).split(",")[1]).split("\\]")[0]);
				    int bottomright_x = Integer.valueOf((position.split("\\[")[2]).split(",")[0]);
				    int bottomright_y = Integer.valueOf(((position.split("\\[")[2]).split(",")[1]).split("\\]")[0]);
				    int width=bottomright_x-upperleft_x;
				    int height=bottomright_y-upperleft_y;
				    curr_widget.setLocationArea(new Rectangle(upperleft_x, upperleft_y, width, height));


				    Node n = node.getAttributes().getNamedItem("resource-id");
				    String widget_id;
				    if (n != null) widget_id = n.getNodeValue();
				    else widget_id = null;
				    //System.out.println("widget id is " + widget_id);

				    curr_widget.putMetadata("id", widget_id);

				    String text = node.getAttributes().getNamedItem("text").getNodeValue();
				    curr_widget.putMetadata("text", text);
				    curr_widget.setText(text);				   

				    
				    String type = node.getAttributes().getNamedItem("class").getNodeValue();
				    curr_widget.putMetadata("type", type);

				    
				    n = node.getAttributes().getNamedItem("content-desc");
				    String content_desc;
				    if (n != null) content_desc = n.getNodeValue();
				    else content_desc = null;
				    curr_widget.putMetadata("content-desc", content_desc);

				    String clickable = node.getAttributes().getNamedItem("clickable").getNodeValue();
				    String checkable = node.getAttributes().getNamedItem("checkable").getNodeValue();
				    String checked = node.getAttributes().getNamedItem("checked").getNodeValue();
				    String scrollable = node.getAttributes().getNamedItem("scrollable").getNodeValue();
				    String focusable = node.getAttributes().getNamedItem("focusable").getNodeValue();
				    String focused = node.getAttributes().getNamedItem("focused").getNodeValue();
				    String long_clickable = node.getAttributes().getNamedItem("long-clickable").getNodeValue();
				    String selected = node.getAttributes().getNamedItem("selected").getNodeValue();
				    
				    curr_widget.putMetadata("clickable", clickable);
				    curr_widget.putMetadata("checkable", checkable);
				    curr_widget.putMetadata("checked", checked);
				    curr_widget.putMetadata("scrollable", scrollable);
				    curr_widget.putMetadata("focusable", focusable);
				    curr_widget.putMetadata("focused", focused);
				    curr_widget.putMetadata("long-clickable", long_clickable);
				    curr_widget.putMetadata("selected", selected);

				    widgetList.add(curr_widget);
				    
				    
			    	curr_widget.setWidgetType(null);


				    if (isTypeWidgetAppium(curr_widget)) {
				    	
				    	curr_widget.setWidgetType(WidgetType.ACTION);
				    	curr_widget.setWidgetSubtype(WidgetSubtype.TYPE_ACTION);
				    	no_type = false;
				    	
				    } 
				    
				    else if (isClickableWidgetAppium(curr_widget)) {
				    	//System.out.println("setting action");
				    	curr_widget.setWidgetType(WidgetType.ACTION);
				    	curr_widget.setWidgetSubtype(WidgetSubtype.LEFT_CLICK_ACTION);
				    	no_type = false;

				    }
				    
				    else if (isLongClickableWidgetAppium(curr_widget)) {
				    	
				    	//USING TRIPLE CLICK AS A LONG CLICK
				    	//BUT ONLY A SINGLE ACTION CAN BE ADDED TO WIDGET TYPE!!!!!
				    	//ASK MICHEL
				    	curr_widget.setWidgetType(WidgetType.ACTION);
				    	curr_widget.setWidgetSubtype(WidgetSubtype.LEFT_CLICK_ACTION);
				    	no_type = false;
				    }
				  
				    
				    else if (isCheckWidgetAppium(curr_widget)) {
				    	//System.out.println(curr_widget.getMetadata("id").toString() + " is check widget");
				    	curr_widget.setWidgetType(WidgetType.CHECK);
						curr_widget.setValidExpression("{text} = "+text.trim());

				    	no_type = false;
				    }

				    //for some reason stuff is not working and everything is click if this one is not left. to debug properly!!!!
				    if (no_type == true) {
				    }

				    
				}

				
		


		
		return widgetList;

		
	}

	
	
	//TODO ALL COOKIES MGMT MISSING WITH APPIUM
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
	

	public ArrayList<String> getAppState(String activity){
		try {
			Process p = Runtime.getRuntime().exec("adb shell dumpsys activity " + activity.split("/")[0].trim());
			/*ProcessBuilder builder = new ProcessBuilder(
					"cmd.exe", "/c", " "adb shell dumpsys activity top");
			Process p= builder.start();*/
			InputStream is = p.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
	        String line, buf = "";
	        while ((line = bufferedReader.readLine()) != null) {
	            buf += line + System.lineSeparator();
	        }
	        ArrayList<String> state = GamificationUtils.parseOutputForFragment(buf, activity);
	        is.close();
	        
	        bufferedReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        while ((line = bufferedReader.readLine()) != null) {
	            System.out.println(line);
	        }
	        bufferedReader.close();
	        System.out.println("il processo è terminato con " +p.waitFor());
	        return state;
		} catch(IOException e) {
			System.err.println("Errore nella ricerca del fragment:" + e.getMessage() );
			System.err.println(e.getStackTrace());
			return null;
		} catch(InterruptedException e) {
			System.err.println("Errore nella wait del processo:" + e.getMessage() );
			System.err.println(e.getStackTrace());
			return null;
		}
	}
	
	public static Session getSession() {
		return thisSession;
	}
	
	/*public boolean updateState(ArrayList<String> st) {
		if(thisState.size() > st.size()) {
			//lo stato salvato è più grande di quello attuale
			if(thisState.containsAll(st)) {
				//se tutto lo stato è contenuto in quello vecchio torno indietro della differenza di dimensione
				thisSession.shrink(thisState.size() - st.size());
			} else {
				// se non tutto lo stato è contenuto ma è comunque più grande
				//onestamente non so che fare, dovrei vedere quanti fragment sono in comune, tornare a quel livello e aggiungere un figlio
				//proviamo a metterlo come fratello
				thisSession.setCurrent(thisSession.getCurrent().getFather());
				thisSession.newNode(st);
				thisState = st;
			}
			
		} else if (thisState.size() == st.size()) {
			//lo stato ha la stessa dimensione
			if(thisState.containsAll(st)) {
				//lo stato è esattamente lo stesso
				return false; //perché non è cambiato
			}
		} else {
			//lo stato salvato ha dimensione minore, quindi ho ampliato
			if(st.containsAll(thisState)) {
				// devo aggiungere un figlio che abbia come dimensione quella nuova
				thisSession.newNode(st);
				thisState= st;
				return true;
			}
		}
		return false;
	}*/
}
