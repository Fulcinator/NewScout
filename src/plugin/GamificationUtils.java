package plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
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
		
		toReturn += "IDENTIFIER ";
		
		if(id != null) {
			toReturn += "ID " + id;
		} else if(name != null) {
			toReturn += "NAME " + name;
		} else if(className != null) {
			toReturn += "CLASSNAME " + className;
		} else if(type != null) {
			toReturn += "TYPE " + type;
		}  else if(href != null) {
			toReturn += "HREF " + href;
		} else {
			String s = value + text + tag;
			toReturn += "HASH " + s.hashCode();
		}	
		
		long time = System.currentTimeMillis();
		toReturn += " TIME " + time;
		return toReturn;
	}
	
	public static void writeSession(Session s) {
		if(s.getTesterId().equals(""))
			return;
		BufferedWriter bw = null;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String tmp = s.getTesterId() + "_" + now.toString() + ".log";
		String filename = "Gamification\\" + s.getTesterId() + "\\" +tmp.replaceAll(":", "_").replaceAll(" ", "_");
		try {
			File directory = new File("Gamification\\" + s.getTesterId());
			
			if(!directory.exists()) {
				directory.mkdirs();
			}
			
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
			File directory = new File("Gamification");
			
			if(!directory.exists()) {
				directory.mkdirs();
			}
			
			File file = new File("Gamification\\" + filename);

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
	
	public static ArrayList<String> loadStats() {
		FileInputStream stream = null;
        try {
        	File myfile = new File("db.txt");
        	
        	if(!myfile.exists()) {
        		System.out.println("Ho creato il file db.txt");
        		myfile.createNewFile();
        	}
            stream = new FileInputStream(myfile.getAbsolutePath());
        } catch (FileNotFoundException e) {
        	//questo catch non dovrebbe mai essere chiamato
        	System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException ioe) {
        	System.err.println("Error in creating file " + ioe.getMessage());
        	ioe.printStackTrace();
        }
        
        if(stream != null) {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	        String strLine;
	        ArrayList<String> lines = new ArrayList<String>();
	        try {
	            while ((strLine = reader.readLine()) != null) {
	                lines.add(strLine);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        try {
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return lines;
        } else {
        	return new ArrayList<>();
        }
    }
	
	public static void parseStats(ArrayList<String> lines, Map<String, Stats> stats) {
		String id = null;
		int min = 0;
		int sec = 0;
		int tothw = 0;
		int issues = 0;
		ArrayList<Double> cov = null;
		ArrayList<Double> eep = null;
		double avgcov = 0.0;
		double avgeep = 0.0;
		
		for(String s : lines) {
			String[] data = s.split(" : ");
			
			if(data[0].equals("STATS"))
				id = data[1];
			
			if(data[0].equals("MIN"))
				min = Integer.parseInt(data[1]);
			
			if(data[0].equals("SEC"))
				sec = Integer.parseInt(data[1]);
			
			if(data[0].equals("HLW"))
				tothw = Integer.parseInt(data[1]);
			
			if(data[0].equals("AVGC"))
				avgcov = Double.parseDouble(data[1]);
			
			if(data[0].equals("ISS"))
				issues = Integer.parseInt(data[1]);
			
			if(data[0].equals("EEP"))
				avgeep = Double.parseDouble(data[1]);
			
			if(data[0].equals("VAL")) {
				String[] avgs = data[1].split("; ");
				cov = new ArrayList<Double>();
				for(String a : avgs)
					cov.add(Double.parseDouble(a));
			}
			
			if(data[0].equals("VAL2")) {
				String[] eeps = data[1].split("; ");
				eep = new ArrayList<Double>();
				for(String e : eeps)
					eep.add(Double.parseDouble(e));
			}
			
			if(data[0].equals("ENDUSER")) {
				Stats st = new Stats(id);
				st.setMinutes(min);
				st.setSeconds(sec);
				st.setHLWidgets(tothw);
				st.setGlobalAvgCoverage(avgcov);
				st.setIssues(issues);
				st.setGlobalEEP(avgeep);
				for(double d : cov)
					st.addAvgCoverage(d);
				for(double e : eep)
					st.addAvgEEP(e);
				stats.put(id, st);
				cov = null;
				eep = null;
			}
		}
	}
	
	public static HashMap<String, String> getNewInteractionInPage(String page, String tester_id) {
		if(!(tester_id.length() > 0))
			return new HashMap<>();
		FileInputStream stream = null;
        try {
        	String reformatPage = page.replaceAll("[<>:/\\\\|?*]","_" );
        	//<>:\\\/\|\?\*
        	File folder = new File("Gamification\\PageInteraction\\" + reformatPage);
        	
        	if(!folder.exists()) {
        		folder.mkdirs();
        	}
        	
        	File myfile = new File("Gamification\\PageInteraction\\" + reformatPage + "\\Widget");
        	
        	if(!myfile.exists()) {
        		myfile.createNewFile();
        	}
            stream = new FileInputStream(myfile.getAbsolutePath());
        } catch (FileNotFoundException e) {
        	//questo catch non dovrebbe mai essere chiamato
        	System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException ioe) {
        	System.err.println("Error in creating file " + ioe.getMessage());
        	ioe.printStackTrace();
        }
        
        if(stream != null) {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	        String strLine;
	        ArrayList<String> lines = new ArrayList<String>();
	        try {
	            while ((strLine = reader.readLine()) != null) {
	                lines.add(strLine);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        HashMap<String, String> map = new HashMap<>();
	        
	        for(String line : lines) {
	        	String[] v = line.split("IDENTIFIER");
	        	if(v == null) {
	        		return null;
	        	}
	        	
	        	String v2[] = v[1].trim().split("FOUND_BY");
	        	if(v2 == null) {
	        		return null;
	        	}
	        	
	        	String widgetId = v2[0].trim();
	        	String finder = v2[1].trim();
	        	map.put(widgetId, finder);
	        }
	        
	        try {
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return map;
        } else {
        	return new HashMap<String, String>();
        }
	}
	
	public static void writeNewInteractionInPage(Session s) {
		if(!(s.getTesterId().length() > 0))
			return;
		BufferedWriter bw = null;
		boolean append = true;
		try {
        	String reformatPage = s.getCurrent().getPage().getId().replaceAll("[<>:/\\\\|?*]","_" );
        	//<>:\\\/\|\?\*
        	File folder = new File("Gamification\\PageInteraction\\" + reformatPage);
        	
        	if(!folder.exists()) {
        		folder.mkdirs();
        	}
        	
        	File myfile = new File("Gamification\\PageInteraction\\" + reformatPage + "\\Widget");
        	
        	if(!myfile.exists()) {
        		myfile.createNewFile();
        		append = false;
        	}
        	append = myfile.length() != 0;
            
        	FileWriter writer = new FileWriter(myfile, append);        
			bw = new BufferedWriter(writer);
			
			HashMap<String, String> map = s.getWidgetNewlyDiscovered();
			
			for(String key : map.keySet()){
				String buffer = "IDENTIFIER " + key + " FOUND_BY " + map.get(key) + System.lineSeparator(); 
				bw.write(buffer);
			}
        	
			bw.close();
        } catch (FileNotFoundException e) {
        	//questo catch non dovrebbe mai essere chiamato
        	System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException ioe) {
        	System.err.println("Error in creating file " + ioe.getMessage());
        	ioe.printStackTrace();
        }			
	}
}
