package plugin;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import scout.AppState;
import scout.StateController;
import scout.Widget;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;

public class SuggestClones
{
	public void updateSuggestions()
	{
		AppState currentState=StateController.getCurrentState();
		List<Widget> suggestions=new ArrayList<Widget>();
		List<String> suggestedValues=new ArrayList<String>();
		List<Widget> widgets=currentState.getAllWidgets();

		// Get clones and mutations of the last performed widget
  	List<Widget> clonesAndMutations=StateController.getClonesAndMutations();
		for(Widget widget:clonesAndMutations)
		{
			// Get the state that is parallel to this
			AppState nextState=widget.getNextState();
			if(nextState!=null)
			{
				// Get widgets available on the sibling state
				List<Widget> siblingStateWidgets=nextState.getVisibleWidgets();
				for(Widget siblingStateWidget:siblingStateWidgets)
				{
					if(siblingStateWidget.getWidgetType()==WidgetType.ACTION)
					{
						if(siblingStateWidget.getWidgetSubtype()==WidgetSubtype.LEFT_CLICK_ACTION)
						{
							Rectangle location=siblingStateWidget.getLocationArea();
							Widget locatedWidget=getWidgetOnLocation(location, widgets);
							if(locatedWidget!=null && locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN)
							{
								if(!containsValue(suggestedValues, siblingStateWidget.getLocationArea().toString()))
								{
									Widget suggestion=new Widget(siblingStateWidget);
									suggestion.setWidgetVisibility(WidgetVisibility.SUGGESTION);
									suggestion.setComment(StateController.translate("SinceSimilarScenario"));
									if(siblingStateWidget.getNextState().isBookmark())
									{
										suggestion.setNextState(siblingStateWidget.getNextState());
									}
									if(suggestions.size()<10)
									{
										suggestions.add(suggestion);
										suggestedValues.add(siblingStateWidget.getLocationArea().toString());
									}
								}
							}
						}
						else if(siblingStateWidget.getWidgetSubtype()==WidgetSubtype.GO_HOME_ACTION)
						{
							if(!containsGoHomeWidget(widgets))
							{
								if(!containsValue(suggestedValues, siblingStateWidget.getLocationArea().toString()))
								{
									Widget suggestion=new Widget(siblingStateWidget);
									suggestion.setWidgetVisibility(WidgetVisibility.SUGGESTION);
									suggestion.setComment(StateController.translate("SinceSimilarScenario"));
									if(siblingStateWidget.getNextState().isBookmark())
									{
										suggestion.setNextState(siblingStateWidget.getNextState());
									}
									if(suggestions.size()<10)
									{
										suggestions.add(suggestion);
										suggestedValues.add(siblingStateWidget.getLocationArea().toString());
									}
								}
							}
						}
						else if(siblingStateWidget.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION || siblingStateWidget.getWidgetSubtype()==WidgetSubtype.SELECT_ACTION)
						{
							String text=siblingStateWidget.getText();
							if(text!=null && text.trim().length()>0)
							{
								Rectangle location=siblingStateWidget.getLocationArea();
								Widget locatedWidget=getWidgetOnLocation(location, widgets);
								if(locatedWidget!=null && locatedWidget.getWidgetVisibility()==WidgetVisibility.HIDDEN)
								{
									if(!containsValue(suggestedValues, siblingStateWidget.getText()))
									{
										Widget suggestion=new Widget(siblingStateWidget);
										suggestion.setWidgetVisibility(WidgetVisibility.SUGGESTION);
										suggestion.setComment(StateController.translate("SinceSimilarScenario"));
										if(siblingStateWidget.getNextState().isBookmark())
										{
											suggestion.setNextState(siblingStateWidget.getNextState());
										}
										if(suggestions.size()<10)
										{
											suggestions.add(suggestion);
											suggestedValues.add(siblingStateWidget.getText());
										}
									}
								}
							}
						}
					}
				}
			}
		}
		currentState.replaceSuggestedWidgets(suggestions, "SuggestClones");
	}
	
	private Widget getWidgetOnLocation(Rectangle location, List<Widget> widgets)
	{
		for(Widget widget:widgets)
		{
			if(widget.getLocationArea()!=null)
			{
				if(location.equals(widget.getLocationArea()))
				{
					return widget;
				}
			}
		}
		return null;
	}

	private boolean containsGoHomeWidget(List<Widget> widgets)
	{
		for(Widget widget:widgets)
		{
			if(widget.getWidgetSubtype()==WidgetSubtype.GO_HOME_ACTION)
			{
				return true;
			}
		}
		return false;
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
