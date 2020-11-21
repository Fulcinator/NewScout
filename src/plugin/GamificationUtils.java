package plugin;

import scout.*;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;

public class GamificationUtils {

	
	public static String logInformation(Widget w) {
		
		String tag=(String)w.getMetadata("tag");
		String className=(String)w.getMetadata("class");
		String type=(String)w.getMetadata("type");
		String name=(String)w.getMetadata("name");
		String id=(String)w.getMetadata("id");
		String href=(String)w.getMetadata("href");
		String value=(String)w.getMetadata("value");
		String text=(String)w.getMetadata("text");
		//Rectangle locationArea=w.getLocationArea();
		
		//System.out.println("tag:" +tag + " - className:" + className + " - type:" + type + " - name:" + name + " - id:" + id + " - value:" + value + " - href:" + href + " - text:" + text);
		String toReturn = "";
		
		if(w.getWidgetType()==WidgetType.CHECK){//check something
			toReturn = "CHECK ";
		} else if(w.getWidgetType()==WidgetType.ISSUE){//issue something
			toReturn = "ISSUE ";
		} else {//it's an action
			if(w.getWidgetSubtype()==WidgetSubtype.TYPE_ACTION){
				int n = 0;
				if(StateController.getKeyboardInput().length() > 0)
					n= StateController.getKeyboardInput().trim().split(" ").length;
				toReturn = "TYPE " + n + " " + StateController.getKeyboardInput() + " ";
			} else if(w.getWidgetSubtype()==WidgetSubtype.LEFT_CLICK_ACTION){
				toReturn = "CLICK ";
			} else if(w.getWidgetSubtype()==WidgetSubtype.SELECT_ACTION){
				toReturn = "SELECT ";
			} else if(w.getWidgetSubtype()==WidgetSubtype.GO_HOME_ACTION){
				toReturn = "GO_HOME ";
			}
			//TODO: controllare se serve fare altri controlli con i sottotipi
		}
		
		
		if(id != null) {
			toReturn += "ID " + id;
		} else if(className != null) {
			toReturn += "CLASSNAME " + className;
		} else if(type != null) {
			toReturn += "TYPE " + type;
		} else if(name != null) {
			toReturn += "NAME " + name;
		} else if(href != null) {
			toReturn += "HREF " + href;
		} else {
			String s = value + text + tag;
			toReturn += "HASH " + s.hashCode();
		}	
		return toReturn;
	}
}
