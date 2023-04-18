import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;

public class TourneyBuilder {
    static final String tournyPrefix = "https://api.challonge.com/v1/tournaments/";
    static final String apiKey = "aGisuEsmAPAHak253ewV65RiCxRCwUnR8iFkIak8";
    static final String apiKeyStart = "220406d16d278620593e1af51a6201bb";
	private static String tourneyPath = "./Database/Tourneys.json";
	private static String changesPath = "./Database/userHistory.json";
	private static Object tourneyObj;
    private static JsonArray tourneyTree;
	private static Object changesObj;
    private static JsonArray changesTree;
	private static JsonParser jsonParser = new JsonParser();
	private static FileWriter file;

	public static void main(String[] args) {
		try {
			//playersTxtToJson();
			txtToJson();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void remakeTourneys() {
    	
    }

    public static void addTourney(String ID, boolean online) throws Exception {
		URL url = new URL(tournyPrefix + ID + "/matches.json?api_key=" + apiKey + "&state=complete");
    	
    	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    	conn.setRequestMethod("GET");
    	conn.connect();

    	int responsecode = conn.getResponseCode();
    	
    	if (responsecode != 200) {
    		System.out.println(tournyPrefix + ID + "/matches.json?api_key=" + apiKey + "&state=complete");
    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	} else {
    		String inline = "";
    	    Scanner scanner = new Scanner(url.openStream());
    	  
    	    while (scanner.hasNext()) {
    	       inline += scanner.nextLine();
    	    }
    	    
    	    scanner.close();

    	    JsonArray matches = (JsonArray) JsonBuilder.jsonParser.parse(inline);
    	    ArrayList<String[]> names = new ArrayList<String[]>();
    	    ArrayList<Integer[]> scores = new ArrayList<Integer[]>();
    	    //System.out.println(Arrays.toString(matches.toArray()));
    	    //System.out.println(matches.size());
    	    for(int i = 0; i < matches.size(); ++i) {
    	    	JsonObject o = (JsonObject) ((JsonObject) matches.get(i)).get("match");
    	    	//System.out.println(((JsonObject) ((JsonObject) matches.get(i)).get("match")).keySet().toString());
    	    	String[] temp = {o.get("player1_id").getAsString(), o.get("player2_id").getAsString()};
    	    	//System.out.println(Arrays.toString(temp));
    	    	String scoreTemp = o.get("scores_csv").getAsString();
    	    	if (scoreTemp.length() == 3) {
    	    		Integer[] temp2 = {scoreTemp.charAt(0) - '0', scoreTemp.charAt(2) - '0'};
    	    		scores.add(temp2);
    	    	} else {
    	    		scores.add( new Integer[] {0, 0} );
    	    	}
    	    	
    	    	names.add(temp);
    	    	
    	    }
    	    
    	    for( int i = 0; i < names.size(); ++i) {
    	    	url = new URL(tournyPrefix + ID + "/participants/" + names.get(i)[0] + ".json?api_key=" + apiKey);
    	    	
    	    	conn = (HttpsURLConnection) url.openConnection();
    	    	conn.setRequestMethod("GET");
    	    	conn.connect();

    	    	responsecode = conn.getResponseCode();
    	    	JsonObject player1;
    	    	JsonObject player2;
    	    	if (responsecode != 200) {
    	    		System.out.println(tournyPrefix + ID + "/participants/" + names.get(i)[0] + ".json?api_key=" + apiKey);
    	    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	    	} else {
    	    		inline = "";
    	    	    scanner = new Scanner(url.openStream());
    	    	  
    	    	    while (scanner.hasNext()) {
    	    	       inline += scanner.nextLine();
    	    	    }
    	    	    scanner.close();

    	    	    player1 = (JsonObject) ((JsonObject) JsonBuilder.jsonParser.parse(inline)).get("participant");
    	    	}
    	    	
    	    	url = new URL(tournyPrefix + ID + "/participants/" + names.get(i)[1] + ".json?api_key=" + apiKey);
    	    	
    	    	conn = (HttpsURLConnection) url.openConnection();
    	    	conn.setRequestMethod("GET");
    	    	conn.connect();

    	    	responsecode = conn.getResponseCode();
    	    	if (responsecode != 200) {
    	    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	    	} else {
    	    		inline = "";
    	    	    scanner = new Scanner(url.openStream());
    	    	    while (scanner.hasNext()) {
    	    	       inline += scanner.nextLine();
    	    	    }
    	    	    scanner.close();
    	    	    player2 = (JsonObject) ((JsonObject) JsonBuilder.jsonParser.parse(inline)).get("participant");
    	    	}
    	    	String[] temp = {player1.get("name").getAsString().toLowerCase(), (String)player2.get("name").getAsString().toLowerCase()};
    	    	names.set(i, temp);
    	    }
    	    
    	    String msg = null;
    	    for(int i = 0; i < scores.size(); ++i) {
    	    	try {
    	    		String name1 = names.get(i)[0];
    	    		String name2 = names.get(i)[1];
    	    		//PlayerBuilder.simulateSet(new StringBuilder(name1), new StringBuilder(name2), scores.get(i)[0], scores.get(i)[1], false, online);
    	    	}
    	    	catch (Exception e) {
    	    		msg = e.getMessage();
    	    		if(msg == null) {
    	    			throw e;
    	    		} else {
    	    			System.out.println(names.get(i)[0] + " " + names.get(i)[1] + " " + scores.get(i)[0] + " " + scores.get(i)[1]);
    	    			System.out.println("\t\t\tError:\t" + msg);
    	    		}
    	    	}
    	    }
    	    if (msg != null) throw new Exception(msg);
    	}
	}

    public static String getTourney(String ID, boolean online) throws Exception {
		String returnMsg = "";
		URL url = new URL(tournyPrefix + ID + "/matches.json?api_key=" + apiKey + "&state=complete");
    	
    	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    	conn.setRequestMethod("GET");
    	conn.connect();

    	int responsecode = conn.getResponseCode();
    	
    	if (responsecode != 200) {
    		System.out.println(tournyPrefix + ID + "/matches.json?api_key=" + apiKey + "&state=complete");
    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	} else {
    		String inline = "";
    	    Scanner scanner = new Scanner(url.openStream());
    	  
    	    while (scanner.hasNext()) {
    	       inline += scanner.nextLine();
    	    }
    	    
    	    scanner.close();

    	    JsonArray matches = (JsonArray) JsonBuilder.jsonParser.parse(inline);
    	    ArrayList<String[]> names = new ArrayList<String[]>();
    	    ArrayList<Integer[]> scores = new ArrayList<Integer[]>();
    	    //System.out.println(Arrays.toString(matches.toArray()));
    	    //System.out.println(matches.size());
    	    for(int i = 0; i < matches.size(); ++i) {
    	    	JsonObject o = (JsonObject) ((JsonObject) matches.get(i)).get("match");
    	    	//System.out.println(((JsonObject) ((JsonObject) matches.get(i)).get("match")).keySet().toString());
    	    	String[] temp = {o.get("player1_id").getAsString(), o.get("player2_id").getAsString()};
    	    	//System.out.println(Arrays.toString(temp));
    	    	String scoreTemp = o.get("scores_csv").getAsString();
    	    	if (scoreTemp.length() == 3) {
    	    		Integer[] temp2 = {scoreTemp.charAt(0) - '0', scoreTemp.charAt(2) - '0'};
    	    		scores.add(temp2);
    	    	} else {
    	    		scores.add( new Integer[] {0, 0} );
    	    	}
    	    	
    	    	names.add(temp);
    	    	
    	    }
    	    
    	    for( int i = 0; i < names.size(); ++i) {
    	    	url = new URL(tournyPrefix + ID + "/participants/" + names.get(i)[0] + ".json?api_key=" + apiKey);
    	    	
    	    	conn = (HttpsURLConnection) url.openConnection();
    	    	conn.setRequestMethod("GET");
    	    	conn.connect();

    	    	responsecode = conn.getResponseCode();
    	    	JsonObject player1;
    	    	JsonObject player2;
    	    	if (responsecode != 200) {
    	    		System.out.println(tournyPrefix + ID + "/participants/" + names.get(i)[0] + ".json?api_key=" + apiKey);
    	    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	    	} else {
    	    		inline = "";
    	    	    scanner = new Scanner(url.openStream());
    	    	  
    	    	    while (scanner.hasNext()) {
    	    	       inline += scanner.nextLine();
    	    	    }
    	    	    scanner.close();

    	    	    player1 = (JsonObject) ((JsonObject) JsonBuilder.jsonParser.parse(inline)).get("participant");
    	    	}
    	    	
    	    	url = new URL(tournyPrefix + ID + "/participants/" + names.get(i)[1] + ".json?api_key=" + apiKey);
    	    	
    	    	conn = (HttpsURLConnection) url.openConnection();
    	    	conn.setRequestMethod("GET");
    	    	conn.connect();

    	    	responsecode = conn.getResponseCode();
    	    	if (responsecode != 200) {
    	    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	    	} else {
    	    		inline = "";
    	    	    scanner = new Scanner(url.openStream());
    	    	    while (scanner.hasNext()) {
    	    	       inline += scanner.nextLine();
    	    	    }
    	    	    scanner.close();
    	    	    player2 = (JsonObject) ((JsonObject) JsonBuilder.jsonParser.parse(inline)).get("participant");
    	    	}
    	    	String[] temp = {player1.get("name").getAsString().toLowerCase(), player2.get("name").getAsString().toLowerCase()};
    	    	names.set(i, temp);
    	    }
    	    String msg = null;
    	    
    	    for(int i = 0; i < scores.size(); ++i) {
    	    	try {
    	    		String name1 = names.get(i)[0];
    	    		String name2 = names.get(i)[1];
    	    		StringBuilder newname1 = new StringBuilder(name1);
    	    		StringBuilder newname2 = new StringBuilder(name2);

    	    		//PlayerBuilder.simulateSet(newname1, newname2, scores.get(i)[0], scores.get(i)[1], false, online);
					name1 = newname1.toString();
    	    		name2 = newname2.toString();
    	    		returnMsg += name1 + "\t" + name2 + "\t" + scores.get(i)[0] + " " + scores.get(i)[1] + "\n";
    	    	}
    	    	catch (Exception e) {
    	    		msg = e.getMessage();
    	    		if(msg == null) {
    	    			throw e;
    	    		} else {
    	    			System.out.println(names.get(i)[0] + " " + names.get(i)[1] + " " + scores.get(i)[0] + " " + scores.get(i)[1]);
    	    			System.out.println("\t\t\tError:\t" + msg);
    	    		}
    	    	}
    	    }
    	    if (msg != null) throw new Exception(msg);
    	    return returnMsg;
    	}
    	    
	}

	public static void replayMatches() throws Exception {
		getJson();
		Iterator<JsonElement> changes = changesTree.iterator();
		JsonArray change = changes.next().getAsJsonArray();
		LocalDate seasonStart = LocalDate.now().minusMonths(6);
		LocalDate time = LocalDate.parse("2001-02-02");
		LocalDate changesTime = LocalDate.parse("2001-01-01");
		Boolean online;
		Boolean thisSeason = false;
		for(JsonElement t : tourneyTree) {
			JsonObject tourney = t.getAsJsonObject();
			time = LocalDate.parse(tourney.get("time").getAsString());
			if (time.isAfter(seasonStart)) {
				thisSeason = true;
			}
			online = tourney.get("online").getAsBoolean();
			while(time.isAfter(changesTime) && changes.hasNext()) {
				changesTime = LocalDate.parse(change.get(0).getAsString());
				String name = change.get(1).getAsString();
				String property = change.get(2).getAsString();
				String value = change.get(3).getAsString();
				PlayerBuilder.changePlayer(name, property, value);
				change = changes.next().getAsJsonArray();
			}
			JsonArray matches = tourney.getAsJsonArray("matches");
			for(JsonElement m : matches) {
				JsonObject match = m.getAsJsonObject();
				String name1 = match.get("name1").getAsString();
				String name2 = match.get("name2").getAsString(); 
				int score1 = match.get("score1").getAsInt();
				int score2 = match.get("score2").getAsInt();
				PlayerBuilder.simulateSet(new StringBuilder(name1), new StringBuilder(name2), score1, score2, online, thisSeason, time);
			}
		}
		changesTime = LocalDate.parse(change.get(0).getAsString());
		String name = change.get(1).getAsString();
		String property = change.get(2).getAsString();
		String value = change.get(3).getAsString();
		PlayerBuilder.changePlayer(name, property, value);
	}

	public static void playersTxtToJson() throws Exception {
		File tempfile = new File(JsonBuilder.playerPath);
		
    	//Players added to Json from players.txt
    	Scanner in = new Scanner(tempfile);
		while (in.hasNextLine()) {
			String[] aliases = {};
			String[] mains = {};
			String[] line = in.nextLine().split(" ", 3);
			if(line.length > 2) aliases = line[2].split("-");
			String town = "none";
			if(Boolean.valueOf(line[1])){ 
				town = "boone";
			}
			PlayerBuilder.addPlayer(line[0].toLowerCase(), town, aliases, mains);
		}
    	in.close();
	}

	public static void txtToJson() throws Exception {
    	PrintWriter pw = new PrintWriter(tourneyPath);
    	pw.print("[]");
    	pw.close();
    	getJson();
    	
    	File tempfile = new File(JsonBuilder.gamesPath);
		Scanner in = new Scanner(tempfile);
    	String[] line;
		JsonObject tempTourney = new JsonObject();
		JsonArray userChange;
		LocalDate time = LocalDate.parse("2001-01-01");
		String[] tempTitle;
		JsonArray matches = null;
    	while (in.hasNextLine()) {
    		line = in.nextLine().toLowerCase().split(" ");
    		if (line.length < 2) {
    			if (line[0].length() > 0) {
    				char cmd = line[0].charAt(0);  //single char commands e.g. "J/player"
    				String name = line[0].substring(2);
					switch (cmd) {
						case '/':
							if (matches != null) {
								tempTourney.add("matches", matches);
								tourneyTree.add(tempTourney);
							}
							matches = new JsonArray();
							tempTourney = new JsonObject();
							tempTitle = name.split(",");
							tempTourney.addProperty("title", tempTitle[0]);
							time = LocalDate.parse(tempTitle[1]);
							break;
						case 'j':
							userChange = new JsonArray();
							userChange.add(time.toString());
							userChange.add(name);
							userChange.add("town");
							userChange.add("boone");
							changesTree.add(userChange);
							break;
						case 'l':
							userChange = new JsonArray();
							userChange.add(time.toString());
							userChange.add(name);
							userChange.add("town");
							userChange.add("none");
							changesTree.add(userChange);
							break;
					}
    			}
    			continue;
    		}
    		boolean online = (line.length == 5) ? Boolean.valueOf(line[4]) : false;
    		StringBuilder name1 = new StringBuilder(line[0]);
			StringBuilder name2 = new StringBuilder(line[1]);
    		PlayerBuilder.getPlayer(name1);
			PlayerBuilder.getPlayer(name2);
    		//simulateSet(new StringBuilder(name1), new StringBuilder(name2), Integer.parseInt(line[2]), Integer.parseInt(line[3]), true, online);  
			JsonObject match = new JsonObject();
			match.addProperty("name1", name1.toString());
			match.addProperty("name2", name2.toString());
			match.addProperty("score1", Integer.parseInt(line[2]));
			match.addProperty("score2", Integer.parseInt(line[3]));
			tempTourney.addProperty("online", online);
			matches.add(match);
    	}
    	in.close();
    	closeFW();
		System.out.println("\nRemake finished");
    }

	private static void getJson() {
		try {
    		tourneyObj = jsonParser.parse(new FileReader(tourneyPath));
    		tourneyTree = ((JsonElement) tourneyObj).getAsJsonArray();
			changesObj = jsonParser.parse(new FileReader(changesPath));
    		changesTree = ((JsonElement) changesObj).getAsJsonArray();
    	}
    	catch(JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
	}

	private static void closeFW() {
    	try {
			file = new FileWriter(tourneyPath);
    		file.write(tourneyTree.toString());
			file.close();
			file = new FileWriter(changesPath);
    		file.write(changesTree.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
