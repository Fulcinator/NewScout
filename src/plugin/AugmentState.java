package plugin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import scout.AppState;
import scout.StateController;
import scout.Widget;
import scout.Widget.WidgetStatus;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;
import sl.shapes.StarPolygon;

public class AugmentState
{
	private static Font textBoxFont = new Font("Arial", Font.PLAIN, 32);
	private static Color textBoxRectangleAuraColor = new Color(255, 255, 255, 160);
	private static Color transparentRedColor = new Color(255, 0, 0, 160);
	private static Color transparentBlueColor = new Color(0, 0, 255, 160);
	private static Color transparentGreenColor = new Color(0, 255, 0, 160);
	private static Color transparentYellowColor = new Color(255, 255, 0, 160);
	private static Color transparentPurpleColor = new Color(255, 0, 255, 160);
	private static Color transparentGreyColor = new Color(160, 160, 160, 160);
	private static BasicStroke stroke=new BasicStroke(3);
	private static BasicStroke auraStroke=new BasicStroke(5);
	private static BasicStroke recommendedStroke=new BasicStroke(7);
	private static BasicStroke recommendedAuraStroke=new BasicStroke(9);
	private static boolean isGamificationActive = true;
	
	public void paintCapture(Graphics g)
	{
		AppState currentState=StateController.getCurrentState();
		Graphics2D g2=(Graphics2D)g;
		int widthW=StateController.getProductViewWidth();
	  	int heightW=StateController.getProductViewHeight();

		if(StateController.isOngoingSession() && currentState!=null)
		{
			List<Widget> allWidgets=currentState.getNonHiddenWidgets();
			allWidgets=StateController.sortWidgets(allWidgets);
			Widget recommendedWidget=StateController.findRecommendedWidget(currentState);
			StateController.setRecommendedWidget(recommendedWidget);
			Widget selectedWidget=StateController.getWidgetAt(StateController.getCurrentState(), StateController.getMouseX(), StateController.getMouseY());

			// Determine the widgets that should be visible
			List<Widget> displayedWidgets=new ArrayList<Widget>();
			for(Widget widget:allWidgets)
			{
				boolean displayWidget=true;
				Rectangle area=widget.getLocationArea();
				if(area!=null && area.contains(StateController.getMouseX(), StateController.getMouseY()))
				{
					// Mouse on widget - only display the selected widget
					if(widget!=selectedWidget)
					{
						// Not the selected one
						displayWidget=false;
					}
				}
				else if(StateController.isOverlapping(widget, recommendedWidget))
				{
					// Overlaps the recommended
					displayWidget=false;
				}
				if(isOverlapping(displayedWidgets, widget))
				{
					displayWidget=false;
				}
				if(displayWidget && widget.getWidgetVisibility()!=WidgetVisibility.HIDDEN)
				{
					displayedWidgets.add(widget);
				}
			}

			// Draw the visible widgets (in reverse order)
			for(int i=displayedWidgets.size()-1; i>=0; i--)
			{
				Widget widget=displayedWidgets.get(i);
				boolean isRecommended=(widget==recommendedWidget);
				Rectangle area=widget.getLocationArea();
				if(widget.getWidgetType()==WidgetType.ACTION)
				{
					if(widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
					{
						// Show performed actions only
						if(widget.getWidgetStatus()==WidgetStatus.LOCATED)
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentBlueColor, isRecommended);
						}
						else
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentGreyColor, isRecommended);
						}
					}
					else if(widget.getWidgetVisibility()==WidgetVisibility.SUGGESTION)
					{
						if(widget.getWidgetStatus()==WidgetStatus.LOCATED)
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentPurpleColor, isRecommended);
						}
						else
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentGreyColor, isRecommended);
						}
					}
				}
				else if(widget.getWidgetType()==WidgetType.CHECK)
				{
					if(widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
					{
						if(widget.getWidgetStatus()==WidgetStatus.VALID)
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentGreenColor, isRecommended);
						}
						else if(widget.getWidgetStatus()==WidgetStatus.LOCATED)
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentYellowColor, isRecommended);
						}
						else
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentGreyColor, isRecommended);
						}
					}
				}
				else if(widget.getWidgetType()==WidgetType.ISSUE)
				{
					if(widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
					{
						if(widget.getWidgetStatus()==WidgetStatus.VALID)
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentRedColor, isRecommended);
						}
						else if(widget.getWidgetStatus()==WidgetStatus.LOCATED)
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentYellowColor, isRecommended);
						}
						else
						{
							drawRectangle(g2, (int)area.getX(), (int)area.getY(), (int)area.getWidth(), (int)area.getHeight(), transparentGreyColor, isRecommended);
						}
					}
				}
			}
			
			if(isGamificationActive) {
				Session s = SeleniumPlugin.getSession();
				if(s == null)
					s = AppiumPlugin.getSession();
				if(s!= null) {
					int width=StateController.getProductViewWidth();
					g2.setColor(transparentRedColor );
					g2.fillRect(0, 0, StateController.getScaledX(width),  StateController.getScaledY(5));
										
					if(s.getCurrent().getPage().getHasEasterEgg()) {
					  	Point p = s.getCurrent().getPage().getEasterEggStartPoint();
					  	if(p != null) {
							Rectangle rect = new Rectangle(p, new Dimension(30, 50));
							drawEgg(g2, rect);
					  	}
					}
					
					Map<String, Double> highscore = s.getCurrent().getPage().getHighscore();
					if(highscore.size() >= 1) {
						for(String key : highscore.keySet()) {
							double blueWidth = width * (highscore.get(key).doubleValue()/ 100);
							g2.setColor(transparentBlueColor );
							g2.fillRect(0, 0, StateController.getScaledX((int) blueWidth),  StateController.getScaledY(5));
						}
					}
					
					Double d = s.getCurrent().getPage().getCoverage();
					double redWidth = d == null ? 0.0 : width * (d.doubleValue()/ 100);
					g2.setColor(transparentGreenColor );
					g2.fillRect(0, 0, StateController.getScaledX((int) redWidth),  StateController.getScaledY(5));
					
					if(s.getCurrent().getPage().isPageNew()) {
						//g2.setColor(Color.YELLOW);
						Color yellow = new Color(255,255,0, 127);
						g2.setColor(yellow);
						double starX = 50;//widthW*0.8;
						double starY = 50;//heightW*0.8;
						StarPolygon stella = new StarPolygon(StateController.getScaledX((int) starX)
								, StateController.getScaledY((int) starY)
								, StateController.getScaledX(30) //big radius
								, StateController.getScaledY(12) //little radius
								, 5, -72);
						g2.fill(stella);
						/*StarPolygon stella1 = new StarPolygon(200, 50, 25, 10, 5, -90);
						g2.fill(stella1);
						StarPolygon stella2 = new StarPolygon(280, 80, 25, 10, 5, -40);
						g2.fill(stella2);
						StarPolygon stella3 = new StarPolygon(280, 160, 25, 10, 5, 40);
						g2.fill(stella3);
						StarPolygon stella4 = new StarPolygon(200, 200, 25, 10, 5, 90);
						g2.fill(stella4);*/
					}
				}
			}
		}
	}

	private void drawEgg(Graphics2D g2, Rectangle rect)
	{
		int x = StateController.getScaledX((int) rect.getX());
		int y = StateController.getScaledY((int) rect.getY());
		int width = StateController.getScaledX((int) rect.getWidth());
		int height = StateController.getScaledY((int) rect.getHeight());

		g2.setStroke(new BasicStroke(1));
		g2.setColor(Color.yellow);
		g2.fillOval(x, y, width, height);
		g2.setColor(Color.black);
		g2.drawOval(x, y, width, height);
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
	 * @param recommended
	 */
	private void drawRectangle(Graphics2D g2, int x, int y, int width, int height, Color color, boolean recommended)
	{
		g2.setFont(textBoxFont);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		x=StateController.getScaledX(x);
		y=StateController.getScaledY(y);
		width=StateController.getScaledX(width);
		height=StateController.getScaledY(height);
		
		int margin=3;

		// Draw white aura
		if(recommended)
		{
			g2.setStroke(recommendedAuraStroke);
		}
		else
		{
			g2.setStroke(auraStroke);
		}
		g2.setColor(textBoxRectangleAuraColor);
		g2.drawRoundRect(x-margin, y-margin, width+margin*2, height+margin*2, 10, 10);

		// Draw rectangle
		g2.setColor(color);
		g2.setStroke(stroke);
		g2.drawRoundRect(x-margin, y-margin, width+margin*2, height+margin*2, 10, 10);

		if(recommended)
		{
			// Draw recommended rectangle
			g2.setStroke(recommendedStroke);
			g2.drawRoundRect(x-margin, y-margin, width+margin*2, height+margin*2, 10, 10);
		}
	}
}
