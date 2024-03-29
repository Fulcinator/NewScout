package plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import scout.StateController;
import scout.Widget;
import scout.Widget.WidgetStatus;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;

public class HoverInfo
{
	private static Font textBoxSmallFont = new Font("Arial", Font.PLAIN, 20);
	private static Color textBoxRectangleBackgroundColor = new Color(0, 0, 0, 160);
	private static Color textBoxRectangleForegroundColor = Color.white;
	private static Color commentForegroundColor = new Color(50, 255, 50);

	public void paintCaptureForeground(Graphics g)
	{
		if(StateController.isOngoingSession() && !StateController.isToolbarVisible())
		{
			Graphics2D g2=(Graphics2D)g;
			int x=StateController.getMouseX();
			int y=StateController.getMouseY();
			String text=getHoverText(x, y);
			if(text!=null)
			{
				drawTextBox(g2, text, x, y, 40, 5);
			}
		}
	}

	/**
	 * @param mouseX
	 * @param mouseY
	 * @return Mouse hover text for mouse location
	 */
	private String getHoverText(int mouseX, int mouseY)
	{
		Widget widget=StateController.getWidgetAt(StateController.getCurrentState(), mouseX, mouseY);
		if(widget!=null)
		{
			if(widget.getWidgetType()==WidgetType.ACTION)
			{
				String action="Unknown";
				String comment=widget.getComment();
				if(widget.getWidgetSubtype()==WidgetSubtype.LEFT_CLICK_ACTION)
				{
					action="Click";
					if(StateController.getKeyboardInput().length()>0)
					{
						// There is keyboard input
						comment="\""+StateController.getKeyboardInput()+"\"";
					}
				}
				else if(widget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION)
				{
					if(StateController.getKeyboardInput().length()>0)
					{
						// There is keyboard input - display that instead
						action="Type \""+StateController.getKeyboardInput()+"\"";
						comment="Click or press enter to type";
					}
					else
					{
						String text=widget.getText();
						if(text!=null && text.trim().length()>0)
						{
							action="Type \""+text+"\"";
						}
						else
						{
							action="Type<br>Enter a text using the keyboard";
						}
					}
				}
				else if(widget.getWidgetSubtype()==WidgetSubtype.SELECT_ACTION)
				{
					if(StateController.getKeyboardInput().length()>0)
					{
						// There is keyboard input
						comment="\""+StateController.getKeyboardInput()+"\"";
					}
					String text=widget.getText();
					if(text!=null && text.trim().length()>0)
					{
						action="Select \""+text+"\"";
					}
					else
					{
						action="Select";
					}
				}
				else if(widget.getWidgetSubtype()==WidgetSubtype.GO_HOME_ACTION)
				{
					action="Go Home";
				}
				if(comment!=null)
				{
					// Add comment
					action+="<br>"+comment;
				}
				return action;
			}
			else if(widget.getWidgetType()==WidgetType.CHECK)
			{
				if(StateController.getKeyboardInput().length()>0 && widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
				{
					// There is keyboard input - display that instead
					return "Report Issue \""+StateController.getKeyboardInput()+"\"<br>Click or press enter to report an issue";
				}
				else
				{
					if(widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
					{
						if(widget.getWidgetStatus()==WidgetStatus.VALID)
						{
							if(widget.getComment()!=null && widget.getComment().trim().length()>0 && widget.getComment().trim().length()<=60)
							{
								return "Check<br>"+widget.getComment();
							}
							else
							{
								return "Check";
							}
						}
						else if(widget.getWidgetStatus()==WidgetStatus.LOCATED)
						{
							if(widget.getComment()!=null && widget.getComment().trim().length()>0 && widget.getComment().trim().length()<=60)
							{
								return "Check<br>"+widget.getComment()+". Enter a comment to report an issue";
							}
							else
							{
								return "Check<br>Enter a comment to report an issue";
							}
						}
						else
						{
							return "Check<br>Enter a comment to report an issue";
						}
					}
					else
					{
						return "Check";
					}
				}
			}
			else if(widget.getWidgetType()==WidgetType.ISSUE)
			{
				if(widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
				{
					return "Issue \""+widget.getReportedText()+"\"<br>Click to resolve the issue";
				}
				else
				{
					return "Issue";
				}
			}
		}
		else
		{
			// No widget selected
			if(StateController.getKeyboardInput().length()>0)
			{
				// There is keyboard input - display that instead
				return "\""+StateController.getKeyboardInput()+"\"<br>Press escape to reset text";
			}
		}
		return null;
	}

	/**
	 * Draw a text box
	 * @param g2
	 * @param text
	 * @param x
	 * @param y
	 * @param deltaY
	 * @param maxCharsPerLine
	 * @param radius
	 */
	private void drawTextBox(Graphics2D g2, String text, int x, int y, int maxCharsPerLine, int radius)
	{
		g2.setFont(textBoxSmallFont);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		x=StateController.getScaledX(x);
		y=StateController.getScaledY(y);

		// Determine if the text box should appear on the left side
		boolean leftSide=false;
		int scaledWidth=StateController.getCaptureScaledWidth();
		if(x>scaledWidth/2)
		{
			leftSide=true;
		}

		FontMetrics fm = g2.getFontMetrics();
		int textHeight = fm.getHeight();
		int textAscent = fm.getMaxAscent();

		int margin=10;
		int marginDist=10;
		int linebreakIndex=0;

		String first=text;
		String last=null;
		int index=text.indexOf("<br>");
		if(index>=0)
		{
			first=text.substring(0, index);
			last=text.substring(index+4);
		}

		List<String> justifiedLines=justify(first, maxCharsPerLine);
		List<String> lines=new ArrayList<String>();
		for(String line:justifiedLines)
		{
			lines.add(line);
		}
		
		linebreakIndex=lines.size();
		
		if(last!=null)
		{
			List<String> justifiedLastLines=justify(last, maxCharsPerLine);
			for(String line:justifiedLastLines)
			{
				lines.add(line);
			}
		}

		int textBoxWidth=getMaxWidth(g2, lines);
		int boxHeight=textHeight+margin*2+(lines.size()-1)*(textHeight+5);

		int boxWidth=textBoxWidth+margin*2;
		int boxStartX;
		int boxStartY;
		int textStartX;
		int textStartY;

		boxStartY=y-boxHeight/2;
		if(boxStartY<0)
		{
			boxStartY=0;
		}
		textStartY=boxStartY+margin*2-textHeight/2+textAscent;
		if(leftSide)
		{
			boxStartX=x-radius-margin-boxWidth-marginDist;
			textStartX=boxStartX+margin;
		}
		else
		{
			boxStartX=x+radius+margin+marginDist;
			textStartX=boxStartX+margin;
		}

		if(boxStartX<0)
		{
			boxStartX=0;
		}
		if(boxStartX+boxWidth>scaledWidth)
		{
			boxStartX=scaledWidth-boxWidth;
		}
		
		// Fill text background
		g2.setColor(textBoxRectangleBackgroundColor);
		g2.fillRect(boxStartX, boxStartY, boxWidth, boxHeight);

		// Draw text
		g2.setColor(textBoxRectangleForegroundColor);
		int lineNo=0;
		for(String line:lines)
		{
			if(linebreakIndex>0 && linebreakIndex==lineNo)
			{
				g2.setColor(commentForegroundColor);
			}
			g2.drawString(line, textStartX, textStartY);
			textStartY+=textHeight+5;
			lineNo++;
		}
	}

	/**
	 * Split a text into lines of max limit length trying not to break words
	 * @param s
	 * @param limit
	 * @return A list of lines
	 */
	private List<String> justify(String s, int limit)
	{
		List<String> lines=new ArrayList<String>();
		StringBuilder justifiedLine = new StringBuilder();
		String[] words = s.split(" ");
		for (int i = 0; i < words.length; i++)
		{
			justifiedLine.append(words[i]).append(" ");
			if (i + 1 == words.length || justifiedLine.length() + words[i + 1].length() > limit)
			{
				justifiedLine.deleteCharAt(justifiedLine.length() - 1);
				String nextLine=justifiedLine.toString();
				List<String> subLines=splitEqually(nextLine, limit);
				for(String subLine:subLines)
				{
					lines.add(subLine);
				}
				justifiedLine = new StringBuilder();
			}
		}
		return lines;
	}

	/**
	 * Split a text into multiple lines with max size
	 * @param text
	 * @param size
	 * @return A number of lines with max size
	 */
	private List<String> splitEqually(String text, int size)
	{
		List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);
		for (int start = 0; start < text.length(); start += size)
		{
			ret.add(text.substring(start, Math.min(text.length(), start + size)));
		}
		return ret;
	}

	/**
	 * @param g2
	 * @param lines
	 * @return The width of the widest line
	 */
	private int getMaxWidth(Graphics2D g2, List<String> lines)
	{
		int maxWidth=0;
		FontMetrics fm = g2.getFontMetrics();
		for(String line:lines)
		{
			int textWidth = fm.stringWidth(line);
			if(textWidth>maxWidth)
			{
				maxWidth=textWidth;
			}
		}
		return maxWidth;
	}
}
