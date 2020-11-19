package plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scout.AppState;
import scout.StateController;
import scout.Widget;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetVisibility;

public class BoundryValueAnalysis
{
	public void updateSuggestions()
	{
		AppState currentState=StateController.getCurrentState();

		List<Widget> suggestions=new ArrayList<Widget>();
		List<String> suggestedValues=new ArrayList<String>();
		List<Widget> widgets=currentState.getVisibleWidgets();
		for(Widget widget:widgets)
		{
			if(widget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION && widget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
			{
				// Get clones and mutations of a visible widget
		  	List<Widget> clonesAndMutations=StateController.getClonesAndMutations(widget);

				// Get values from all clones and mutations
				List<String> allValues=new ArrayList<String>();
				for(Widget cloneWidget:clonesAndMutations)
				{
					allValues.add(cloneWidget.getText());
				}
				
				if(allValues.size()>0)
				{
					// We have previous values
					if(isIntList(allValues))
					{
						// Integers
						final String[] integerSuggestions={"-1", "0", "1"};
						for(String integerSuggestion:integerSuggestions)
						{
							if(!containsValue(allValues, integerSuggestion) && !containsValue(suggestedValues, integerSuggestion))
							{
								// Suggestion not found among current values
								Widget suggestion=new Widget(widget);
								suggestion.setWidgetVisibility(WidgetVisibility.SUGGESTION);
								suggestion.setText(integerSuggestion);
								suggestion.setComment(StateController.translate("SinceBoundryValue"));
								suggestions.add(suggestion);
								suggestedValues.add(integerSuggestion);
							}
						}
					}
					else if(isEmail(allValues))
					{
						// Emails
						List<String> emailSuggestions=new ArrayList<String>();
						String firstEmail=allValues.get(0);
						int index=firstEmail.indexOf('@');
						if(index>=0)
						{
							String withoutName=firstEmail.substring(index);
							emailSuggestions.add(withoutName);
						}
						int lastIndex=firstEmail.lastIndexOf('.');
						if(lastIndex>=0)
						{
							String withoutDomain=firstEmail.substring(0, lastIndex);
							emailSuggestions.add(withoutDomain);
						}
						for(String emailSuggestion:emailSuggestions)
						{
							if(!containsValue(allValues, emailSuggestion) && !containsValue(suggestedValues, emailSuggestion))
							{
								Widget suggestion=new Widget(widget);
								suggestion.setWidgetVisibility(WidgetVisibility.SUGGESTION);
								suggestion.setText(emailSuggestion);
								suggestion.setComment(StateController.translate("SinceNegativeTest"));
								suggestions.add(suggestion);
								suggestedValues.add(emailSuggestion);
							}
						}
					}
				}
			}
		}
		if(suggestions.size()>0)
		{
			currentState.replaceSuggestedWidgets(suggestions, "BoundryValueAnalysis");
		}
	}

	private boolean isIntList(List<String> list)
	{
		for(String item:list)
		{
			try
			{
				Integer.parseInt(item.trim());
			}
			catch(Exception e)
			{
				// Not an int
				return false;
			}
		}
		return true;
	}

	private boolean isEmail(List<String> list)
	{
		for(String item:list)
		{
			if(!isEmail(item))
			{
				return false;
			}
		}
		return true;
	}

	private boolean isEmail(String email)
	{
		Pattern pattern = Pattern.compile("^.+@.+\\..+$");
		Matcher m = pattern.matcher(email);
		return m.matches();
	}
	
	private boolean containsValue(List<String> list, String value)
	{
		for(String item:list)
		{
			if(item.trim().equals(value.trim()))
			{
				return true;
			}
		}
		return false;
	}
}
