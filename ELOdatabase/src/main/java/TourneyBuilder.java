import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;

import javax.net.ssl.HttpsURLConnection;
import java.util.Set;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;

public class TourneyBuilder {
    static final String tournyPrefix = "https://api.challonge.com/v1/tournaments/";
    static String apiKey;
    static String apiKeyStart;
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
			getPlayersFromMatches();
			//playersTxtToJson();
			//txtToJson();
			//addTourneyStart("tournament/buddwaur-s-birthday-bash-3-holiday-edition/event/melee-singles", "bbb3", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void addTourney(String link, String ID, String name, boolean online, LocalDate time, JsonArray tourneyMatches) {
    	JsonObject tourney = new JsonObject();
		tourney.addProperty("link", link);
		tourney.addProperty("id", ID);
		tourney.addProperty("time", time.toString());
		tourney.addProperty("online", online);
		tourney.addProperty("title", name);
		tourney.add("matches", tourneyMatches);
		if(tourneyTree.size() >= 1) {
			JsonArray tempTree = new JsonArray(tourneyTree.size() + 1);
			Iterator<JsonElement> treeCurs = tourneyTree.iterator();
			JsonObject tempTourney = treeCurs.next().getAsJsonObject();
			LocalDate tempDate = LocalDate.parse(tempTourney.get("time").getAsString());
			while(treeCurs.hasNext() && time.isAfter(tempDate)) {
				tempTree.add(tempTourney);
				tempTourney = treeCurs.next().getAsJsonObject();
				tempDate = LocalDate.parse(tempTourney.get("time").getAsString());
			}
			if(time.isAfter(tempDate)) {
				tempTree.add(tempTourney);
				tempTourney = null;
			}
			tempTree.add(tourney);
			while(treeCurs.hasNext()) {
				tempTree.add(tempTourney);
				tempTourney = treeCurs.next().getAsJsonObject();
			}
			if(tempTourney != null)
				tempTree.add(tempTourney);
			tourneyTree = tempTree;
		} else {
			tourneyTree.add(tourney);
		}
		closeFW();
    }



	public static void addTourneyStart(String link, String name, boolean online) throws Exception {
		if (tourneyObj == null) getJson();
		JsonArray tourneyMatches = new JsonArray();
		JsonParser jsonParser = new JsonParser();
		
		OkHttpClient client = new OkHttpClient().newBuilder()  //Postman code.
        										.build();
    	String pages = "1";
    	String perPage = "100";
    	MediaType mediaType = MediaType.parse("application/json");
    	String json = "{\"query\":\"query EventSets($eventSlug: String!, $page: Int!, $perPage: Int!) {\\r\\n  event(slug: $eventSlug) {\\r\\n    id\\r\\n    startAt\\r\\n    name\\r\\n    sets(\\r\\n      page: $page\\r\\n      perPage: $perPage\\r\\n      sortType: STANDARD\\r\\n    ) {\\r\\n      pageInfo {\\r\\n        total\\r\\n      }\\r\\n      nodes {\\r\\n        id\\r\\n        winnerId\\r\\n        displayScore\\r\\n        totalGames\\r\\n        slots {\\r\\n          id\\r\\n          entrant {\\r\\n            id\\r\\n            name\\r\\n          }\\r\\n        }\\r\\n      }\\r\\n    }\\r\\n  }\\r\\n},\",\"variables\":{\"eventSlug\":\""+ link + "\",\"page\":" + pages + ",\"perPage\":"+ perPage +"}}";
    	RequestBody body = RequestBody.create(mediaType, json);
    	Request request = new Request.Builder()
    	  .url("https://api.smash.gg/gql/alpha")
    	  .method("POST", body)
    	  .addHeader("Authorization", "Bearer " + apiKeyStart)
    	  .addHeader("Content-Type", "application/json")
    	  .build();
    	Response response = client.newCall(request).execute(); //end of Postman code
		String jsonResponse = response.body().string();	
    	//System.out.println(jsonResponse);
    	
		JsonObject event = jsonParser.parse(jsonResponse).getAsJsonObject().get("data").getAsJsonObject().get("event").getAsJsonObject();
		long id = event.get("id").getAsLong();
		long unixStartAt = event.get("startAt").getAsLong() * 1000;
		LocalDate tourneyStart = Instant.ofEpochMilli(unixStartAt).atZone(ZoneId.systemDefault()).toLocalDate();
    	JsonArray sets = event.get("sets").getAsJsonObject().get("nodes").getAsJsonArray();
    	for(JsonElement set : sets) {
    		JsonObject setO = set.getAsJsonObject();
    		String scoreLine = setO.get("displayScore").getAsString();
    		if (scoreLine.equals("DQ")) {
    			continue;
    		}
    		//String winnerID = setO.get("winnerId").getAsString();
    		//int games = setO.get("totalGames").getAsInt();
    		int Lscore = Character.getNumericValue(scoreLine.charAt(scoreLine.length()-1));
    		int Wscore = Character.getNumericValue(scoreLine.charAt(scoreLine.indexOf(" - ") - 1));
		
    		JsonObject entrant1 = setO.get("slots").getAsJsonArray().get(0).getAsJsonObject().get("entrant").getAsJsonObject();
    		JsonObject entrant2 = setO.get("slots").getAsJsonArray().get(1).getAsJsonObject().get("entrant").getAsJsonObject();
    		//int id1 = entrant1.get("id").getAsInt();
    		//int id2 = entrant2.get("id").getAsInt();
    		String name1 = entrant1.get("name").getAsString();
    		String name2 = entrant2.get("name").getAsString();
    		if (Lscore > Wscore) {
    			int t = Lscore;
    			Lscore = Wscore;
    			Wscore = t;
    			String tempName = name1;
    			name1 = name2;
    			name2 = tempName;
    		}
			JsonObject match = new JsonObject();
			match.addProperty("name1", name1);
			match.addProperty("name2", name2);
			match.addProperty("score1", Wscore);
			match.addProperty("score2", Lscore);
    		tourneyMatches.add(match);
    	}
		
		addTourney(link, String.valueOf(id), name, online, tourneyStart, tourneyMatches);
	}

    public static void addTourneyChallonge(String ID, String name, boolean online, String link) throws Exception {
		getJson();
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

			url = new URL(tournyPrefix + ID + "/participants.json?api_key=" + apiKey);
    	    	
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			responsecode = conn.getResponseCode();

			if (responsecode != 200) {
				System.out.println(tournyPrefix + ID + "/participants.json?api_key=" + apiKey);
				throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {
				inline = "";
				scanner = new Scanner(url.openStream());
				while (scanner.hasNext()) {
				   inline += scanner.nextLine();
				}
				scanner.close();

				LocalDate dateObj = LocalDate.parse( matches.get(0).getAsJsonObject().get("match").getAsJsonObject().get("started_at").getAsString().substring(0, 10));
				JsonArray participants = JsonBuilder.jsonParser.parse(inline).getAsJsonArray();
				HashMap<String, String> nameMap = new HashMap<String, String>();
				for( int k = 0; k < participants.size(); ++k) {
					JsonObject p = participants.get(k).getAsJsonObject().get("participant").getAsJsonObject();
					String pName = p.get("name").getAsString();
					String pID = p.get("id").getAsString();
					JsonArray groups = (JsonArray)p.get("group_player_ids");
					if(groups != null) {
						for (JsonElement g : groups) {
							nameMap.put(g.getAsString(), pName);
						}
					}
					nameMap.put(pID, pName);
				}

				ArrayList<String[]> names = new ArrayList<String[]>();
				ArrayList<Integer[]> scores = new ArrayList<Integer[]>();
				//System.out.println(Arrays.toString(matches.toArray()));
				//System.out.println(matches.size());
				for(int i = 0; i < matches.size(); ++i) {
					JsonObject o = (JsonObject) ((JsonObject) matches.get(i)).get("match");
					//System.out.println(((JsonObject) ((JsonObject) matches.get(i)).get("match")).keySet().toString());
					String[] temp = {nameMap.get(o.get("player1_id").getAsString()), nameMap.get(o.get("player2_id").getAsString())};
					//System.out.println(Arrays.toString(temp));
					String scoreTemp = o.get("scores_csv").getAsString();
					//System.out.println("scores: " + scoreTemp);
					Integer[] temp2 = {scoreTemp.charAt(0) - '0', scoreTemp.charAt(2) - '0'};
					if (scoreTemp.length() == 3 && temp2[0] + temp2[1] > 0) {
						
						scores.add(temp2);
						names.add(temp);
					} 
				}
				JsonArray tourneyMatches = new JsonArray();
				String msg = null;
				for(int i = 0; i < scores.size(); ++i) {
					try {
						String name1 = names.get(i)[0];
						String name2 = names.get(i)[1];
						int score1 = scores.get(i)[0];
						int score2 = scores.get(i)[1];
						//PlayerBuilder.simulateSet(newname1, newname2, score1, score2, online, dateObj);
						JsonObject match = new JsonObject();
						match.addProperty("name1", name1);
						match.addProperty("name2", name2);
						match.addProperty("score1", score1);
						match.addProperty("score2", score2);
						tourneyMatches.add(match);
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
				addTourney(link, ID, name, online, dateObj, tourneyMatches);
			}
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

	public static void getPlayersFromMatches() throws Exception {
		if (tourneyObj == null) getJson();
		for(JsonElement t : tourneyTree) {
			JsonObject nameChanges = new JsonObject();
			JsonObject tourney = t.getAsJsonObject();
			for (JsonElement match : tourney.get("matches").getAsJsonArray()) {
				JsonObject m = match.getAsJsonObject();
				String name1 = m.get("name1").getAsString();
				String name2 = m.get("name2").getAsString();
				StringBuilder tempName1 = new StringBuilder(name1);
				StringBuilder tempName2 = new StringBuilder(name2);
				PlayerBuilder.getPlayer(tempName1);
				PlayerBuilder.getPlayer(tempName2);
				if(!tempName1.toString().equals(name1)) {
					m.addProperty("name1", tempName1.toString());
					nameChanges.addProperty(tempName1.toString(), name1);
				}
				if(!tempName2.toString().equals(name2)) {
					m.addProperty("name2", tempName2.toString());
					nameChanges.addProperty(tempName2.toString(), name2);
				}
			}
			tourney.add("name changes", nameChanges);
			closeFW();
		}
		
	}

	public static void replayMatches() throws Exception {
		getJson();
		Iterator<JsonElement> changes = changesTree.iterator();
		JsonArray change = changes.next().getAsJsonArray();
		LocalDate time = LocalDate.parse("2001-02-02");
		LocalDate changesTime = LocalDate.parse(change.get(0).getAsString());
		Boolean online;
		Set<String> missingPlayers = new HashSet<String>();

		for(JsonElement t : tourneyTree) {
			JsonObject tourney = t.getAsJsonObject();
			time = LocalDate.parse(tourney.get("time").getAsString());
			online = tourney.get("online").getAsBoolean();
			while(time.isAfter(changesTime)) {
				String name = change.get(1).getAsString();
				String property = change.get(2).getAsString();
				String value = change.get(3).getAsString();
				PlayerBuilder.changePlayer(name, property, value);
				/*if(property.equals("town")) {
					JsonArray elos = new JsonArray();
					PlayerBuilder.changePlayer(name, "elos", elos);
				}*/
				if(changes.hasNext()){
					change = changes.next().getAsJsonArray();
					changesTime = LocalDate.parse(change.get(0).getAsString());
				} else {
					changesTime = LocalDate.now();
				}
			}
			JsonArray matches = tourney.getAsJsonArray("matches");
			for(JsonElement m : matches) {
				JsonObject match = m.getAsJsonObject();
				String name1 = match.get("name1").getAsString();
				String name2 = match.get("name2").getAsString(); 
				int score1 = match.get("score1").getAsInt();
				int score2 = match.get("score2").getAsInt();
				try {
					PlayerBuilder.simulateSet(new StringBuilder(name1), new StringBuilder(name2), score1, score2, online, time);
				} catch(Exception e) {
					missingPlayers.add(e.getMessage());
				}
			}
		}
		for(String err : missingPlayers) {
			System.out.println("Error in replays: " + err);
		}
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
							tempTourney.addProperty("time", time.toString());
							if(tempTitle.length > 2)
								tempTourney.addProperty("link", tempTitle[2]);
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

	public static ArrayList<String[]> getUpcomingMatches(String ID) throws Exception {
    	URL url = new URL(tournyPrefix + ID + "/matches.json?api_key=" + apiKey + "&state=open");
    	
    	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    	conn.setRequestMethod("GET");
    	conn.connect();

    	int responsecode = conn.getResponseCode();
    	
    	if (responsecode != 200) {
    		System.out.println(tournyPrefix + ID + "/matches.json?api_key=" + apiKey + "&state=open");
    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	} else {
    		String inline = "";
    	    Scanner scanner = new Scanner(url.openStream());
    	  
    	    while (scanner.hasNext()) {
    	       inline += scanner.nextLine();
    	    }
    	    
    	    scanner.close();

    	    JsonArray matches = jsonParser.parse(inline).getAsJsonArray();
    	    ArrayList<String[]> matchIDs = new ArrayList<String[]>();
    	    //System.out.println(Arrays.toString(matches.toArray()));
    	    //System.out.println(matches.size());
    	    for(int i = 0; i < matches.size(); ++i) {
    	    	JsonObject o = (JsonObject) ((JsonObject) matches.get(i)).get("match");
    	    	//System.out.println(((JsonObject) ((JsonObject) matches.get(i)).get("match")).keySet().toString());
    	    	String[] temp = {o.get("player1_id").getAsString(), o.get("player2_id").getAsString(),  o.get("id").getAsString()};
    	    	//System.out.println(Arrays.toString(temp));
    	    	matchIDs.add(temp);
    	    }
    	    
    	    for( int i = 0; i < matchIDs.size(); ++i) {
    	    	url = new URL(tournyPrefix + ID + "/participants/" + matchIDs.get(i)[0] + ".json?api_key=" + apiKey);
    	    	
    	    	conn = (HttpsURLConnection) url.openConnection();
    	    	conn.setRequestMethod("GET");
    	    	conn.connect();

    	    	responsecode = conn.getResponseCode();
    	    	JsonObject player1;
    	    	JsonObject player2;
    	    	if (responsecode != 200) {
    	    		System.out.println(tournyPrefix + ID + "/participants/" + matchIDs.get(i)[0] + ".json?api_key=" + apiKey);
    	    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	    	} else {
    	    		inline = "";
    	    	    scanner = new Scanner(url.openStream());
    	    	  
    	    	    while (scanner.hasNext()) {
    	    	       inline += scanner.nextLine();
    	    	    }
    	    	    scanner.close();

    	    	    player1 = (JsonObject) ((JsonObject) jsonParser.parse(inline)).get("participant");
    	    	}
    	    	
    	    	url = new URL(tournyPrefix + ID + "/participants/" + matchIDs.get(i)[1] + ".json?api_key=" + apiKey);
    	    	
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
    	    	    player2 = (JsonObject) ((JsonObject) jsonParser.parse(inline)).get("participant");
    	    	}
    	    	String[] temp = { (player1.get("name").getAsString()), player2.get("name").getAsString(), matchIDs.get(i)[2] };
    	    	matchIDs.set(i, temp);
    	    }
    	    return matchIDs;
    	}
    }
}
