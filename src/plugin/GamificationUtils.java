package plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

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
		
		long time = System.currentTimeMillis();
		toReturn += "TIME " + time;
		return toReturn;
	}
	
	public static void writeSession(Session s) {
		if(s.getTesterId().equals(""))
			return;
		BufferedWriter bw = null;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String tmp = s.getTesterId() + "_" + now.toString() + ".log";
		String filename = tmp.replaceAll(":", "_").replaceAll(" ", "_");
		try {
			File file = new File(filename);

			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter writer = new FileWriter(file);
			bw = new BufferedWriter(writer);
			
			List<String> l = s.getRoot().obtainPages();
			
			for(String page : l){
				bw.write(page);
			}
			
			bw.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.err.println("An error occurred." + e.getMessage());
			File myObj = new File(filename); 
			if (myObj.delete()) { 
				System.out.println("Deleted the file: " + myObj.getName());
			} else {
				System.out.println("Failed to delete the file.");
			}
			e.printStackTrace();
		}
	}
	
	public static void saveStats(Map<String, Stats> stats) {
		BufferedWriter bw = null;
		String filename = "db.txt";
		try {
			File file = new File(filename);

			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter writer = new FileWriter(file);
			bw = new BufferedWriter(writer);
			
			for(Stats st: stats.values())
				bw.write(st.prepareStats());
			
			bw.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.err.println("An error occurred." + e.getMessage());
			File myObj = new File(filename); 
			if (myObj.delete()) { 
				System.out.println("Deleted the file: " + myObj.getName());
			} else {
				System.out.println("Failed to delete the file.");
			}
			e.printStackTrace();
		}
	}
}
