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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	public static String parseOutputForaActivity(String output) throws ParseException {
		String toReturn = "";
		
		if(output.contains("java.io.IOException"))
			throw new ParseException("LEZZO", 0);
		
		String s1 = output.substring(0, output.indexOf("Local Activity"));
		String s2 = s1.substring(s1.indexOf("ACTIVITY")).trim();
		toReturn = s2.split("/")[1].trim().split(" ")[0].trim();
		return toReturn;
	}
	
	public static ArrayList<String> parseOutputForFragment(String output, String activityName) throws ParseException {
		if(output.contains("java.io.IOException"))
			throw new ParseException("LEZZO", 0);
		ArrayList<String> fragments = new ArrayList<>();
		boolean everythingFine = false;
		String sub1 =
				//output.split( "ACTIVITY " + activityName.split("/")[0].trim());
				output.substring(output.indexOf("ACTIVITY " + activityName));
		
		int i1 = sub1.indexOf("Local FragmentActivity");
		if(i1 != -1) {
			String sub2 = sub1.substring(i1);
			
			int i2 = sub2.indexOf("Added Fragments:");
			if(i2 != -1) {
				String sub3 = sub2.substring(i2);
				
				String v1[] = sub3.split("Back Stack")[0].split("Added Fragments:");
				if(v1.length > 1) {
					String sub4 = v1[1].trim();
					String s = sub4.split("Fragments Created Menus:")[0].trim();
					String [] vet = s.split("#[0-9]:");
					
					for(int i = 1; i < vet.length; i++) {
						String frag = vet[i].substring(0,vet[i].indexOf("{")).trim();
						fragments.add(frag);
					}
					everythingFine = true;
				}
			}
		}
		
		if(everythingFine)
			return fragments;
		else {
			String sub3 = sub1.substring(sub1.indexOf("Added Fragments:"));
			String sub4 = sub3.split("FragmentManager")[0].trim();
			String [] vet = sub4.split("#[0-9]:");
			
			for(int i = 1; i < vet.length; i++) {
				String frag = vet[i].substring(0,vet[i].indexOf("{")).trim();
				fragments.add(frag);
			}
			return fragments;
		}
	}
	
	public static String logInformationAndroid(Widget w) {
		
		String type=(String)w.getMetadata("type");
		String name=(String)w.getMetadata("name");
		String id=(String)w.getMetadata("id");
		String value=(String)w.getMetadata("value");
		String text=(String)w.getMetadata("text");
		
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
		}  else if(value != null || text != null){
			String s = value + text;
			toReturn += "HASH " + s.hashCode();
		}  else  {
			toReturn += "TYPE " + type;
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
	
	public static void savePages(ArrayList<String> pages, String filename) {
		BufferedWriter bw = null;
		
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
			
			for(String p: pages)
				bw.write(p + System.lineSeparator());
			
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
	
	public static ArrayList<String> loadStats(String path) {
		FileInputStream stream = null;
        try {
        	File directory = new File("Gamification");
			
			if(!directory.exists()) {
				directory.mkdirs();
			}
			
			File myfile = new File("Gamification\\" + path);
        	
        	if(!myfile.exists()) {
        		System.out.println("Ho creato il file " + path);
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
		/**
		 * chiamato dal costruttore di statsComputer
		 */
		String id = null;
		int min = 0;
		int sec = 0;
		int tothw = 0;
		int issues = 0;
		int newpages = 0;
		int newwidgets = 0;
		int score = 0;
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
			
			if(data[0].equals("NEWW"))
				newwidgets = Integer.parseInt(data[1]);
			
			if(data[0].equals("NEWP"))
				newpages = Integer.parseInt(data[1]);
			
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
			
			if(data[0].equals("SCO"))
				score = Integer.parseInt(data[1]);
			
			if(data[0].equals("ENDUSER")) {
				Stats st = new Stats(id);
				st.setMinutes(min);
				st.setSeconds(sec);
				st.setHLWidgets(tothw);
				st.setGlobalAvgCoverage(avgcov);
				st.setIssues(issues);
				st.setGlobalEEP(avgeep);
				st.setNewWidgets(newwidgets);
				st.setNewPages(newpages);
				st.setTotScore(score);
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
	
	public static Map<String, Double> getHighScorePage(String page){
		String reformatPage = page.replaceAll("[<>:/\\\\|?*]","_" );
    	File folder = new File("Gamification\\PageInteraction\\" + reformatPage);
    	
    	if(!folder.exists()) {
    		folder.mkdirs();
    	}
    	
    	File myfile = new File("Gamification\\PageInteraction\\" + reformatPage + "\\Highscore");
    	
    	if(!myfile.exists()) {
    		return new HashMap<String, Double>();
    	}
    	
    	FileInputStream stream = null;
    	try {
			stream = new FileInputStream(myfile.getAbsolutePath());
			
		} catch (FileNotFoundException e) {
			// impossibile perch� c'� il controllo prima
			e.printStackTrace();
		};
		
		HashMap<String, Double> highscore = new HashMap<String, Double>();
		
		if(stream != null) {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	        String strLine;
	        try {
	            while ((strLine = reader.readLine()) != null) {
	            	String key = strLine.split("VALUE")[0].trim().split("USER")[1].trim();
	            	Double value = Double.parseDouble(strLine.split("VALUE")[1].trim());
	                highscore.put(key, value);
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		return highscore;
	}
	
	public static void writeHighScorePage(Page page) {
		String reformatPage = page.getId().replaceAll("[<>:/\\\\|?*]","_" );
    	//<>:\\\/\|\?\*
    	File folder = new File("Gamification\\PageInteraction\\" + reformatPage);
    	
    	if(!folder.exists()) {
    		folder.mkdirs();
    	}
    	
    	File myfile = new File("Gamification\\PageInteraction\\" + reformatPage + "\\Highscore");
    	
    	if(!myfile.exists()) {
    		try {
				myfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	FileWriter writer = null;
		try {
			writer = new FileWriter(myfile);
			if(writer != null) {
				BufferedWriter bw = new BufferedWriter(writer);
			
				for(String p: page.getHighscore().keySet()) {
					String buf = "USER " + p + " VALUE " + page.getHighscore().get(p) + System.lineSeparator();
					bw.write(buf);
				}
				
				bw.close();
			}
		
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	public static Map<String,String> parseStatsForRecap(Stats st) {
		Map<String,String> toReturn = new LinkedHashMap<>();
		DecimalFormat df = new DecimalFormat("#.#");
		
		toReturn.put("Tester", st.getTesterId());
		toReturn.put("Minutes", String.valueOf(st.getMinutes()));
		toReturn.put("Seconds", String.valueOf(st.getSeconds()));
		toReturn.put("Highlighted Widgets", String.valueOf(st.getTotHLWidgets()));
		toReturn.put("Average Coverage", String.valueOf(df.format(st.getGlobalAvgCoverage()) + "%"));
		toReturn.put("Issues", String.valueOf(st.getIssues()));
		toReturn.put("Easter Eggs Found (%)", String.valueOf(df.format(st.getGlobalEEPercentage()) + "%"));
		toReturn.put("Discovered Pages", String.valueOf(st.getNewPages()));
		toReturn.put("Discovered Widgets", String.valueOf(st.getNewWidgets()));
		toReturn.put("Score", String.valueOf(st.getScore()));
		toReturn.put("Bonus Score", String.valueOf(st.getBonus()));
		toReturn.put("Grade", st.getGrade());
		
		return toReturn;
	}
	
	public static Map<String,Integer> getLeaderboard() {
		Map<String,Integer> leaderboard = new LinkedHashMap<>();
		ArrayList<String> stats = loadStats("db.txt");
		String user = "";
		int score = 0;
		
		for(String s : stats) {
			String[] data = s.split(" : ");
			
			if(data[0].equals("STATS"))
				user += data[1];
			
			if(data[0].equals("SCO"))
				score = Integer.parseInt(data[1]);
			
			if(data[0].equals("ENDUSER")) {
				leaderboard.put(user, score);
				user = "";
				score = 0;
			}
		}
		
		Map<String,Integer> toReturn = leaderboard.entrySet().stream()
			       						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			       						.limit(10)
			       						.collect(Collectors.toMap(
			       								Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
		return toReturn;
		
	}
	
	public static String getUserList(Map<String,Integer> leaderboard) {
		String toReturn = "Testers\r\n\r\n";
		int position = 1;
		Iterator<Map.Entry<String, Integer>> iterator = leaderboard.entrySet().iterator();
		while (iterator.hasNext()) {
	        Map.Entry<String, Integer> entry = iterator.next();
	        toReturn += position + ".  " + entry.getKey() + "\r\n\r\n";
	        position++;
		}
		
		return toReturn;
	}
	
	public static String getScoreList(Map<String,Integer> leaderboard) {
		String toReturn = "Points\r\n\r\n";
		Iterator<Map.Entry<String, Integer>> iterator = leaderboard.entrySet().iterator();
		while (iterator.hasNext()) {
	        Map.Entry<String, Integer> entry = iterator.next();
	        toReturn += entry.getValue() + "\r\n\r\n";
		}
		
		return toReturn;
	}
	
	public static Map<String,String> getStats() {
		String id = StateController.getTesterName();
		ArrayList<String> stats = loadStats("db.txt");
		Map<String,String> toReturn = new LinkedHashMap<>();
		boolean found = false;
		
		for(String s : stats) {
			String[] data = s.split(" : ");
			
			if(data[0].equals("STATS") && data[1].equals(id))
				found = true;
			
			if(!data[0].equals("ENDUSER") && found)
				toReturn.put(data[0], data[1]);
			
			if(data[0].equals("ENDUSER"))
				found = false;
		}
		
		return toReturn;
	}
	
	public static Map<String,Double> parseCoeff(ArrayList<String> values) {
		Map<String,Double> toReturn = new HashMap<>();
		
		for(String s : values) {
			String[] data = s.split(" : ");
			toReturn.put(data[0], Double.parseDouble(data[1]));
		}
		
		toReturn = checkCoeff(toReturn);
		
		return toReturn;
	}
	
	public static Map<String,Double> checkCoeff(Map<String,Double> toCheck) {
		boolean correct = false;
		Map<String,Double> toReturn = new HashMap<>();
		
		if(toCheck.size() == 8)
			correct = true;
		
		if(correct) {
			double a = toCheck.get("a");
			double k = toCheck.get("k");
			double h = toCheck.get("h");
			double c = toCheck.get("c");
			double d = toCheck.get("d");
			double x = toCheck.get("x");
			double y = toCheck.get("y");
			double z = toCheck.get("z");
			int alfa = 1;
			int beta = 1;
			int gamma = 3;
			
			if((a+k+h+c) != 100.00 || (d+x+y+z) != 50 || ((x*100) % alfa) != 0 || ((y*100) % beta) != 0 || ((z*100) % gamma) != 0)
				correct = false;
			else
				return toCheck;
		}
		
		if(!correct) {
			double a = 0.6;
			double k = 0.1;
			double h = 0.2;
			double c = 0.1;
			double d = 0.25;
			double x = 0.05;
			double y = 0.05;
			double z = 0.15;
			
			toReturn.put("a", a);
			toReturn.put("k", k);
			toReturn.put("h", h);
			toReturn.put("c", c);
			toReturn.put("d", d);
			toReturn.put("x", x);
			toReturn.put("y", y);
			toReturn.put("z", z);
		}
		
		return toReturn;
	}
}
