package plugin;

import java.util.List;

import scout.StateController;
import scout.Widget;

public class UpdateWidgetLocation
{
	public void updateState()
	{
		List<Widget> nonHiddenWidgets=StateController.getCurrentState().getNonHiddenWidgets();
		for(Widget widget:nonHiddenWidgets)
		{
			Widget matchingWidget=(Widget)widget.getMetadata("matching_widget");
			if(matchingWidget!=null)
			{
				// Has a matching widget
				if(!widget.getLocationArea().equals(matchingWidget.getLocationArea()))
				{
					// Update location and size
					widget.setLocationArea(matchingWidget.getLocationArea());
				}
			}
		}
	}
}
