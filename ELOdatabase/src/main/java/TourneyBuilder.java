import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TourneyBuilder {
    static final String tournyPrefix = "https://api.challonge.com/v1/tournaments/";
    static final String apiKey = "aGisuEsmAPAHak253ewV65RiCxRCwUnR8iFkIak8";
    static final String apiKeyStart = "220406d16d278620593e1af51a6201bb";

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
    	    		PlayerBuilder.simulateSet(new StringBuilder(name1), new StringBuilder(name2), scores.get(i)[0], scores.get(i)[1], false, online);
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

    	    		PlayerBuilder.simulateSet(newname1, newname2, scores.get(i)[0], scores.get(i)[1], false, online);
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
}
