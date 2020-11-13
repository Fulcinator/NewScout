package plugin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.util.List;

import scout.StateController;
import scout.Widget;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;

public class AugmentCoverage
{
	private static BasicStroke thinStroke=new BasicStroke(1);
	private static BasicStroke mediumStroke=new BasicStroke(4);
	private static Color transparentRedColor = new Color(255, 0, 0, 160);
	
	/**
	 * Called by the PluginController when time to draw overlay graphics
	 * @param g
	 */
	public void paintCaptureForeground(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;

		if(StateController.isOngoingSession())
		{
			// Draw total coverage
			int percentCovered = StateController.getStateTree().coveredPercent(StateController.getProductVersion());
			if(percentCovered>0)
			{
				int visibleY=StateController.getVisibleY();
				int visibleHeight=StateController.getVisibleHeight();
				drawOval(g2, 50, visibleY+visibleHeight-50, 20, 20, transparentRedColor, percentCovered);
			}
			
			if(!StateController.isToolbarVisible())
			{
				int mouseX=StateController.getMouseX();
				int mouseY=StateController.getMouseY();
				int mouseScaledX=StateController.getMouseScaledX();
				int mouseScaledY=StateController.getMouseScaledY();

				List<Widget> widgets=StateController.getWidgetsAt(StateController.getCurrentState(), mouseX, mouseY);
				if(widgets.size()>0)
				{
					// Draw coverage and arrows of widget hovering over
					Widget widget=widgets.get(0);
					boolean severalOptions=widgets.size()>1;
					if(widget.getWidgetType()==WidgetType.ACTION)
					{
						if(widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
						{
							percentCovered = widget.coveredPercent(StateController.getProductVersion(), true);
							if(percentCovered>0)
							{
								drawOval(g2, mouseScaledX, mouseScaledY, 20, 20, transparentRedColor, percentCovered);
							}
						}
						if(severalOptions || widget.getWidgetSubtype()==WidgetSubtype.SELECT_ACTION)
				    {
				    	// Draw arrows
							drawArrows(g2, mouseScaledX, mouseScaledY, 10, 10, transparentRedColor);
				    }
					}
				}
			}
		}
	}

	private void drawOval(Graphics2D g2, int centerX, int centerY, int radiusX, int radiusY, Color color, int percentCovered)
	{
		int width = radiusX * 2;
		int height = radiusY * 2;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setColor(color);

		int pieDegree = (360 * percentCovered) / 100;
		if (pieDegree == 360)
		{
			g2.setStroke(mediumStroke);
			g2.drawOval(centerX - radiusX*2, centerY - radiusY*2, width*2, height*2);
		}
		else if (pieDegree >= 0)
		{
			Arc2D.Float arc = new Arc2D.Float(Arc2D.PIE);
			arc.setFrame(centerX - radiusX * 2, centerY - radiusY * 2, width * 2, height * 2);
			arc.setAngleStart(90);
			arc.setAngleExtent(-pieDegree);
			g2.setStroke(mediumStroke);
			g2.draw(arc);
			g2.setStroke(thinStroke);
			g2.drawOval(centerX - radiusX*2, centerY - radiusY*2, width*2, height*2);
		}
	}

	private void drawArrows(Graphics2D g2, int centerX, int centerY, int radiusX, int radiusY, Color color)
	{
		// Draw up arrow
		g2.setColor(color);
		g2.setStroke(mediumStroke);
  	g2.drawLine(centerX - radiusX, centerY-5 - radiusY, centerX, centerY-5 - radiusY*2);
  	g2.drawLine(centerX + radiusX, centerY-5 - radiusY, centerX, centerY-5 - radiusY*2);

  	// Draw down arrow
  	g2.drawLine(centerX - radiusX, centerY+5 + radiusY, centerX, centerY+5 + radiusY*2);
  	g2.drawLine(centerX + radiusX, centerY+5 + radiusY, centerX, centerY+5 + radiusY*2);
	}
}
