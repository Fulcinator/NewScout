package plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import scout.Action;
import scout.AppState;
import scout.DoubleClickAction;
import scout.DragAction;
import scout.DragDropAction;
import scout.DragStartAction;
import scout.GoHomeAction;
import scout.LeftClickAction;
import scout.MiddleClickAction;
import scout.MouseScrollAction;
import scout.RemoveAction;
import scout.RightClickAction;
import scout.StateController;
import scout.TripleClickAction;
import scout.TypeAction;
import scout.Widget;

public class HelloWorld
{
	private static Font textBoxFont = new Font("Arial", Font.PLAIN, 32);

	/**
	 * Called when the plugin is enabled
	 */
	public void enablePlugin()
	{
		StateController.displayMessage("Plugin Enabled");
	}

	/**
	 * Called when the plugin is disabled
	 */
	public void disablePlugin()
	{
	}

	/**
	 * Called when the session begins
	 */
	public void startSession()
	{
		StateController.displayMessage("Session Started");
	}

	/**
	 * Called when the session is stopped
	 */
	public void stopSession()
	{
	}

	/**
	 * Called when the session is paused
	 */
	public void pauseSession()
	{
		StateController.displayMessage("Session Paused");
	}

	/**
	 * Called when the session is resumed after being paused
	 */
	public void resumeSession()
	{
		StateController.displayMessage("Session Resumed");
	}

	/**
	 * Called when the state changes
	 */
	public void changeState()
	{
		StateController.displayMessage("Changed State");
	}

	/**
	 * Called periodically to update the state
	 */
	public void updateState()
	{
	}

	/**
	 * Update the suggestions given by this plugin
	 */
	public void updateSuggestions()
	{
		AppState currentState=StateController.getCurrentState();
		List<Widget> suggestions=new ArrayList<Widget>();
		currentState.replaceSuggestedWidgets(suggestions, "HelloWorld");
	}

	/**
	 * Called when the user performs an action like a mouse or keyboard event
	 * @param action
	 */
	public void performAction(Action action)
	{
		if(!StateController.isRunningSession())
		{
			// Only perform actions during a running session
			return;
		}

		if(action instanceof GoHomeAction)
		{
		}

		if(action instanceof RemoveAction)
		{
		}

		if(action instanceof MouseScrollAction)
		{
		}

		if(action instanceof TypeAction)
		{
		}
		
		if(action instanceof LeftClickAction)
		{
		}
		
		if(action instanceof MiddleClickAction)
		{
		}
		
		if(action instanceof RightClickAction)
		{
		}
		
		if(action instanceof DoubleClickAction)
		{
		}
		
		if(action instanceof TripleClickAction)
		{
		}

		if (action instanceof DragStartAction)
		{
		}

		if (action instanceof DragAction)
		{
		}

		if (action instanceof DragDropAction)
		{
		}
	}

	/**
	 * Called when a report should be generated
	 * Adding this method will display this plugin in the Reports drop-down
	 */
	public void generateReport()
	{
		// Make sure we have a reports folder
		File file=new File("reports/"+StateController.getProduct());
		file.mkdirs();
		
		// Log one line to the report
		String filename="reports/"+StateController.getProduct()+"/hello.txt";
		log(filename, "Hello World");

		StateController.displayMessage("Generating report");
	}

	/**
	 * Called by the PluginController when time to draw graphics
	 * @param g
	 */
	public void paintCapture(Graphics g)
	{
	}

	/**
	 * Called by the PluginController when time to draw foreground graphics
	 * @param g
	 */
	public void paintCaptureForeground(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.white);
		g2.setFont(textBoxFont);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		String text="Hello Everybody";
		FontMetrics fm = g2.getFontMetrics();
		int textHeight = fm.getHeight();
		int textWidth = fm.stringWidth(text);
		int x=StateController.getVisibleWidth()/2-textWidth/2;
		int y=StateController.getVisibleY()+StateController.getVisibleHeight()/2-textHeight/2;
		g2.drawString(text, x, y);
	}

	/**
	 * Save the state tree for the current product
	 * @return true if done
	 */
/*
	public Boolean saveState()
	{
		AppState stateTree=StateController.getStateTree();
		String product=StateController.getProduct();
		return true;
	}
*/
	
	/**
	 * Load state tree for the current product or create a new home state if not found
	 * @return A state tree
	 */
/*
	public AppState loadState()
	{
		String product=StateController.getProduct();
		return new AppState("0", "Home");
	}
*/

	private void log(String logFilename, String message)
	{
		writeLine(logFilename, message, true);
	}

	private void writeLine(String filename, String text, boolean append)
	{
		String logMessage = text + "\r\n";
		File file = new File(filename);
		try
		{
			FileOutputStream o = new FileOutputStream(file, append);
			o.write(logMessage.getBytes());
			o.close();
		}
		catch (Exception e)
		{
		}
	}
}
