package plugin;

import java.awt.Point;
import java.util.List;

import scout.AppState;
import scout.LeftClickAction;
import scout.PluginController;
import scout.StateController;
import scout.StateController.Mode;
import scout.StateController.Route;
import scout.Widget;
import scout.Widget.WidgetStatus;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;

public class Autopilot
{
	private static long changedStateTime=0;
	private static AppState currentState=null;
	private static boolean hasPerformedAction=false;

	public void startSession()
	{
		currentState=StateController.getCurrentState();
		hasPerformedAction=false;
	}

	public void changeState()
	{
		changedStateTime=System.currentTimeMillis();
		currentState=StateController.getCurrentState();
		hasPerformedAction=false;
	}

	public void updateState()
	{
		if(!hasPerformedAction)
		{
			// Has not performed action since state changed
			autoPerformAction();
		}
	}

	private void autoPerformAction()
	{
		if(StateController.getMode()==Mode.AUTO)
		{
//		if(isReadyToPerformAction() && System.currentTimeMillis()>changedStateTime+5000)
			if(isReadyToPerformAction())
			{
				Widget recommendedWidget=StateController.findRecommendedWidget(currentState);
				if(recommendedWidget!=null)
				{
					if(recommendedWidget.getWidgetType()==WidgetType.ACTION && recommendedWidget.getWidgetVisibility()==WidgetVisibility.VISIBLE)
					{
						// Click on the recommended action
						if(currentState==StateController.getCurrentState())
						{
							LeftClickAction action=new LeftClickAction();
							Point p=new Point((int)recommendedWidget.getLocationArea().getCenterX(), (int)recommendedWidget.getLocationArea().getCenterY());
							action.setLocation(p);
							action.setCreatedByPlugin("Autopilot");
							PluginController.performAction(action);
							hasPerformedAction=true;
						}
					}
				}
			}

			if(StateController.getRoute()==Route.NAVIGATING && StateController.getNavigationTargetState()!=null && StateController.getNavigationTargetState()==StateController.getCurrentState())
			{
				// Is navigating and is a target state
				StateController.displayMessage("Reached the destination");
				StateController.setMode(Mode.MANUAL);
				StateController.setRoute(Route.EXPLORING);
				return;
			}

			long duration=System.currentTimeMillis()-changedStateTime;
			int coveredPercent=StateController.getStateTree().coveredPercent(StateController.getProductVersion());
			if(coveredPercent==100 && StateController.getRoute()!=Route.NAVIGATING)
			{
				// Done
				StateController.displayMessage(coveredPercent+"% coverage");
				StateController.setMode(Mode.MANUAL);
			}
			else if(changedStateTime!=0 && duration>10000)
			{
				// Waited a while to find a recommended action - give up
				StateController.displayMessage(coveredPercent+"% coverage");
				StateController.setMode(Mode.MANUAL);
			}
		}
		else
		{
			// In manual mode - reset time
			changedStateTime=System.currentTimeMillis();
		}
	}
	
	/**
	 * @return true if ready to auto perform an action
	 */
	private boolean isReadyToPerformAction()
	{
		if(currentState!=null)
		{
			List<Widget> widgets=currentState.getVisibleWidgets();
			if(widgets==null || widgets.size()==0)
			{
				return false;
			}
			for(Widget widget:widgets)
			{
				if((widget.getWidgetType()==WidgetType.ACTION && widget.getWidgetStatus()==WidgetStatus.LOCATED) || 
						(widget.getWidgetType()==WidgetType.CHECK && widget.getWidgetStatus()==WidgetStatus.VALID) || 
						(widget.getWidgetType()==WidgetType.ISSUE && (widget.getWidgetStatus()==WidgetStatus.VALID || widget.getWidgetStatus()==WidgetStatus.UNLOCATED)))
				{
					// Located or valid
				}
				else
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
