
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;

public class JsonBuilder {
	static JsonParser jsonParser = new JsonParser();
	static Object obj;
    static JsonObject jsonTree;
    static FileWriter file;
    public static int[] tourneyCounts = new int[100];
    public static int currSeason = 2;
    public static final String[] jsonPaths = {"./Database/spring2022DB.json", "./Database/fall2021DB.json", "./Database/allDB.json"};
    public static String jsonPath = jsonPaths[currSeason];
    public static final String[] gamesPaths = {"./Database/spring2022Games.txt", "./Database/fall2021Games.txt", "./Database/allGames.txt"};
    public static String gamesPath = gamesPaths[currSeason];
    public static final String playerPath = "./Database/Players.txt";
    static final int startELO = 1000;
    static final int maxELO = 9999;
    static final double startRD = 350;
    static final double startVol = 0.06;
    static final String tournyPrefix = "https://api.challonge.com/v1/tournaments/";
    static final String apiKey = "aGisuEsmAPAHak253ewV65RiCxRCwUnR8iFkIak8";
    static final String apiKeyStart = "220406d16d278620593e1af51a6201bb";
    
    public static double[] accuracy = new double[20];
    public static double[] accuracy2 = new double[20];
    public static int[] accuracy3 = new int[20];
    public static int totalGames = 0;
    public static double accTotal = 0.0;
    public static int correct;
    
    public static void main(String[] args) {
    	
    	System.out.println("Working Directory = " + System.getProperty("user.dir"));
    	try {
    		//addTourney("10974919", true);
			remakeFile();
			//System.out.println(getRankings());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /*
     * Order of names does not matter as long as the scores match.
     * 		i.e. "bups G 2 0" is the same as "G bups 0 2"
     *
     * returns false for wrong names or scores.
     */
    public static void simulateSet(StringBuilder sbName1, StringBuilder sbName2, int score1, int score2, boolean remake, boolean online) throws Exception {
		getJson();
		String name1 = sbName1.toString();
		String name2 = sbName2.toString();
		
		JsonObject obj1 = getPlayer(sbName1);
		JsonObject obj2 = getPlayer(sbName2);
		if (obj1 == null) throw new Exception(name1);
		if (obj2 == null) throw new Exception(name2);
		name1 = sbName1.toString();
		name2 = sbName1.toString();
		
		if (score1 < 0 || score2 < 0 || (score1 == 0 && score2 == 0) || (score1 + score2 > 5)) throw new Exception("Scores");
		if (name1.equals(name2)) throw new Exception("duplicate");
		for(int i = 0; i < score1; ++i) {
			simulateGame(name1, name2, online);
		}
		for(int i = 0; i < score2; ++i) {
			simulateGame(name2, name1, online);
		}
		
		JsonObject matchup;
		JsonObject matchup2;
		if ((matchup = (JsonObject) obj1.get(name2)) == null) {
			matchup = new JsonObject();
			matchup.addProperty("W", score1);
			matchup.addProperty("L", score2);
		} else {
			int w = (matchup.get("W").getAsNumber()).intValue();
			int l = (matchup.get("L").getAsNumber()).intValue();
			matchup.addProperty("W", w + score1);
			matchup.addProperty("L", l + score2);
		}
		if ((matchup2 = (JsonObject) obj2.get(name1)) == null) {
			matchup2 = new JsonObject();
			matchup2.addProperty("W", score2);
			matchup2.addProperty("L", score1);
		} else {
			int w = (matchup2.get("W").getAsNumber()).intValue();
			int l = (matchup2.get("L").getAsNumber()).intValue();
			matchup2.addProperty("W", w + score2);
			matchup2.addProperty("L", l + score1);
		}
		obj1.add(name2, matchup);
		obj2.add(name1, matchup2);
		jsonTree.add(name1, obj1);
		jsonTree.add(name2, obj2);
		
		getFW();
		closeFW();
		if(remake) return; //Update the games json file for new set
		try {
			file = new FileWriter(gamesPath, true);
			file.append("\n" + name1 + " " + name2 + " " + score1 + " " + score2 + " " + online);
			file.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
    
    public static double hotness(String name1, String name2) throws Exception {
    	if (obj == null) getJson();
    	StringBuilder newname1 = new StringBuilder(name1);
		StringBuilder newname2 = new StringBuilder(name2);
		JsonObject obj1 = (JsonObject) getPlayer(newname1);
		JsonObject obj2 = (JsonObject) getPlayer(newname2);
		name1 = newname1.toString();
		name2 = newname2.toString();
		if (obj1 == null) throw new Exception(name1);
		if (obj2 == null) throw new Exception(name2);
		if (name1.equals(name2)) throw new Exception("duplicate");
		
		double winnerELO = obj1.get("ELO").getAsDouble();
		double loserELO = obj2.get("ELO").getAsDouble();
		int winnerGames = (obj1.get("games").getAsNumber()).intValue();
		int loserGames = (obj2.get("games").getAsNumber()).intValue();
		double eloOdds = calcELO(winnerELO, loserELO, winnerGames, loserGames, false)[2];
		JsonObject matchup = (JsonObject) obj1.get(name2);
		if (matchup != null) {
			int w = (matchup.get("W").getAsNumber()).intValue();
			double total = w + matchup.get("L").getAsDouble();
			double realOdds = (w) / total;
			System.out.print(realOdds + " ");
			return 10 * (eloOdds + realOdds ) / 2; 
		} 
		return 10 * eloOdds;
    }
    
    public static int[] getRecord(String name1, String name2) throws Exception {
    	getJson();
    	StringBuilder newname1 = new StringBuilder(name1);
		StringBuilder newname2 = new StringBuilder(name2);
		JsonObject obj1 = (JsonObject) getPlayer(newname1);
		JsonObject obj2 = (JsonObject) getPlayer(newname2);
		name1 = newname1.toString();
		name2 = newname2.toString();
		if (obj1 == null) throw new Exception(name1);
		if (obj2 == null) throw new Exception(name2);
		if (name1.equals(name2)) throw new Exception("duplicate");
		JsonObject matchup = (JsonObject) obj1.get(name2);
		if (matchup == null) {
			throw new Exception("none");
		} else {
			int[] record = new int[3];
			record[1] = (matchup.get("W").getAsNumber()).intValue();
			record[2] = (matchup.get("L").getAsNumber()).intValue();
			record[0] = record[1] + record[2];
			return record;
		} //TODO: make this return online and offline records as well as sets
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
    
    public static boolean changePlayer(String name, String key, Object value) {
    	if (obj == null) getJson();
    	StringBuilder n = new StringBuilder(name);
    	JsonObject player = getPlayer(n);
    	name = n.toString();
    	if (player == null) return false;
    	getFW();
		JsonElement val = jsonParser.parse(value.toString());
		player.add(key, val);
    	jsonTree.add(name, player);
    	closeFW();
    	return true;
    }

    public static JsonObject getPlayer(StringBuilder sb) {
		if (obj == null) getJson();
    	String name = sb.toString().toLowerCase();
    	if (!jsonTree.has(name)) {
    		String xname = name.replaceAll("\\p{Punct}", "");
    		if (jsonTree.has(xname)) {
    			name = xname;
    		} else {
				String sname = name.replaceAll("\\p{Punct}", " ");
				for(String s : sname.split(" ")) {
					if (jsonTree.has(s)) {
						name = s;
					}
				}
    		}
		}
    	JsonElement player = jsonTree.get(name);

		if (player == null) return null;

    	if (player.isJsonPrimitive()) {
    		sb.replace(0, sb.length(), player.getAsString());
    		player = jsonTree.get( player.getAsString());
    	} 
    	else {
    		sb.replace(0, sb.length(), name); }
    	return (JsonObject) player;
    }
    /*
     * returns false if changes failed
     */
    public static boolean deletePlayer(String name) {
    	if (obj == null) getJson();
    	if (!jsonTree.has(name)) return false;
    	jsonTree.remove(name);
    	getFW();
    	closeFW();
    	return true;
    }

    /*
     * 
     */
	public static void addPlayer(String name, boolean local, String[] aliases, boolean remake) throws Exception {
		if (obj == null) getJson();
		if (jsonTree.has(name)) throw new Exception(name);
		
		for(String s : aliases) {
			if (jsonTree.has(s)) throw new Exception(s);
			jsonTree.addProperty(s.toLowerCase(), name);
		}
		
		getFW();
		JsonObject newPlayer = new JsonObject();
		//newPlayer.addProperty("name", name);
		newPlayer.addProperty("ELO", startELO);
		newPlayer.addProperty("games", 0);
		newPlayer.addProperty("local", local);
		newPlayer.addProperty("ELORD", startRD);
		newPlayer.addProperty("ELOVol", startVol);
		newPlayer.addProperty("currTourney", 0);
		newPlayer.addProperty("lastTourney", 0);
		jsonTree.add(name, newPlayer);
		closeFW();
		
		getJson();
		if (remake) return;
		
		FileWriter playerWrite = new FileWriter(playerPath, true);
		playerWrite.append("\n" + name + " " + local + " ");
		
		if(aliases.length > 0) {
			playerWrite.append(aliases[0]);
		}
		playerWrite.close();
	}

    public static String getRankings() {
    	List<String> list = new ArrayList<>();
    	if (obj == null) getJson();
    	for (String key : jsonTree.keySet()) {
    		if (jsonTree.get(key).isJsonPrimitive()) continue;
    		list.add( key);
    	}
    	Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String p1, String p2) {
               return -1 * Double.compare(jsonTree.getAsJsonObject(p1).get("ELO").getAsDouble(), jsonTree.getAsJsonObject(p2).get("ELO").getAsDouble());
            }
        });
    	String answer = "Current rankings: ```\n";

    	for(String p : list) {
    		JsonObject pObj = (JsonObject) jsonTree.get(p);
    		if ( pObj.get("games").getAsInt() >= 10 && (pObj.get("local").getAsBoolean())) {
    			answer += String.format("%-10s\t%d\n", p, (pObj).get("ELO").getAsInt());
    		}
    	}
    	answer += "```Out of towners (their games don't affect local ELO):```\n";
    	for(String p : list) {
    		JsonObject pObj = (JsonObject) jsonTree.get(p);
    		if ( pObj.get("games").getAsInt() >= 10 && !(pObj.get("local").getAsBoolean())) {
    			answer += String.format("%-10s\t%d\n", p, (pObj).get("ELO").getAsInt());
    		}
    	}
    	answer += "```\n";
    	return answer;
    }
    
    public static void remakeTourneys() {
    	
    }
    
    public static void remakeFile() throws Exception {
    	PrintWriter pw = new PrintWriter(jsonPath);
    	pw.print("{}");
    	pw.close();
    	getJson();
    	File tempfile = new File(playerPath);
		
    	//Players added to Json from players.txt
    	Scanner in = new Scanner(tempfile);
		while (in.hasNextLine()) {
			String[] aliases = {};
			String[] line = in.nextLine().split(" ");
			if(line.length > 2) aliases = line[2].split("-");
			addPlayer(line[0].toLowerCase(), Boolean.valueOf(line[1]), aliases, true);
		}
    	in.close();
    	
    	tempfile = new File(gamesPath);
    	in = new Scanner(tempfile);
    	String[] line;
    	while (in.hasNextLine()) {
    		line = in.nextLine().toLowerCase().split(" ");
    		if (line.length < 2) {
    			if (line[0].length() > 0) {
    				char cmd = line[0].charAt(0);  //single char commands e.g. "J/player"
    				String name = line[0].substring(2);
					switch (cmd) {
						case '/':
							++tourneyCounts[currSeason];
							break;
						case 'j':
							changePlayer(name, "local", true);
							break;
						case 'l':
							changePlayer(name, "local", false);
							break;
					}
    			}
    			continue;
    		}
    		boolean online = (line.length == 5) ? Boolean.valueOf(line[4]) : false;
    		
    		String name1 = line[0];
    		String name2 = line[1]; 
    		simulateSet(new StringBuilder(name1), new StringBuilder(name2), Integer.parseInt(line[2]), Integer.parseInt(line[3]), true, online);    		
    	}
    	in.close();
    	
    	//System.out.printf("%.4f ", accTotal / totalGames);
    	//System.out.printf("%.2f%%", (100 * (double) correct) / totalGames);
    	for (int i = 0; i < 20; ++i) {
    		//System.out.printf("%.3f\n", (accuracy[i] / accuracy2[i]));
    	} 
    	for (int i = 0; i < 20; ++i) {
    		//System.out.printf("%d\n", accuracy3[i]);
    	}
		System.out.println("\nRemake finished");
    }
    
    public static String getAliases(String name) throws Exception {
    	if (obj == null) getJson();
		if (jsonTree.get(name).isJsonPrimitive()) name = jsonTree.get(name).getAsString();
    	File tempfile = new File(playerPath);
    	Scanner in = new Scanner(tempfile);
    	while (in.hasNextLine()) {
			String[] line = in.nextLine().split(" ");
			if(line[0].equalsIgnoreCase(name)) {
				String ans = name;
				if (line.length > 2) {
					for (String s : line[2].split("-")) {
						ans += ", " + s;
					}
				}
				return ans;
			}
		}
    	in.close();
    	throw new Exception("none");
    }
    
    private static void getJson() {
    	try {
    		obj = jsonParser.parse(new FileReader(jsonPath));
    		jsonTree = (JsonObject)obj;
    	}
    	catch(JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void getFW() {
    	try {
    		file = new FileWriter(jsonPath);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    private static void closeFW() {
    	try {
    		file.write(jsonTree.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	private static void simulateGame(String name1, String name2, boolean online) {
		JsonObject winner = (JsonObject) jsonTree.get(name1);
		JsonObject loser = (JsonObject) jsonTree.get(name2);
		
		int winnerCurr = winner.get("currTourney").getAsInt();
		int winnerLast = winner.get("currTourney").getAsInt();
		int winnerTime;
		if (tourneyCounts[currSeason] != winnerCurr) {
			winnerTime = tourneyCounts[currSeason] - winnerCurr;
			winner.addProperty("currTourney", tourneyCounts[currSeason]);
			winner.addProperty("lastTourney", winnerCurr);
		} else {
			winnerTime = tourneyCounts[currSeason] - winnerLast;
		}
		int loserCurr = loser.get("currTourney").getAsInt();
		int loserLast = loser.get("currTourney").getAsInt();
		int loserTime;
		if (tourneyCounts[currSeason] != loserCurr) {
			loserTime = tourneyCounts[currSeason] - loserCurr;
			loser.addProperty("currTourney", tourneyCounts[currSeason]);
			loser.addProperty("lastTourney", loserCurr);
		} else {
			loserTime = tourneyCounts[currSeason] - loserLast;
		}
		double winnerELO = winner.get("ELO").getAsDouble();
		double loserELO =  loser.get("ELO").getAsDouble();
		int winnerGames =  winner.get("games").getAsInt();
		int loserGames = loser.get("games").getAsInt();
		double winnerRD =  winner.get("ELORD").getAsDouble();
		double winnerVol = winner.get("ELOVol").getAsDouble();
		double loserRD = loser.get("ELORD").getAsDouble();
		double loserVol =  loser.get("ELOVol").getAsDouble();
		//double[] newELOs = calcELO(winnerELO, loserELO, winnerRD, loserRD, winnerVol, loserVol, winnerTime, loserTime);
		double[] newELOs = calcELO(winnerELO, loserELO, winnerGames, loserGames, online);
		boolean winnerLocal = winner.get("local").getAsBoolean();
		boolean loserLocal = loser.get("local").getAsBoolean();

		if((!winnerLocal || loserLocal) && (loserGames > 3 || winnerGames <= 3)) {
			winner.addProperty("ELO", newELOs[0]);
			winner.addProperty("games", winnerGames + 1);
			jsonTree.add(name1, winner);
		}
		if((!loserLocal || winnerLocal) && (winnerGames > 3 || loserGames <= 3)) {
			loser.addProperty("ELO", newELOs[1]);
			loser.addProperty("games", loserGames + 1);
			jsonTree.add(name2, loser);
		}
	}
	
	private static double[] calcELO(double winnerELO, double loserELO, int winnerGames, int loserGames, boolean online) {
		double[] ans = new double[3];
		double loseExpected = 1.0 / (1 + Math.pow(10, (winnerELO - loserELO)/400));
		double winExpected = 1.0 - loseExpected;
		ans[2] = winExpected;
		double winK = Math.max((150.0 / (winnerGames + 1)), 36.0);
		
		//double winK = 96;
		double loseK = Math.max((150.0 / (loserGames + 1)), 36.0);
		if (online) {
			winK *= 0.75;
			loseK *= 0.75;
		}
		double winNew = (winnerELO + winK * (1 - winExpected));
		ans[0] = winNew;
		//double loseK = 96;
		double loseNew = (loserELO + loseK * (0 - loseExpected));
		ans[1] = loseNew;
		
		//System.out.println(1-winExpected);
		
		if (!online && tourneyCounts[currSeason] > 28) {
			++accuracy[(int) (winExpected * 20)];
			++accuracy2[(int) (winExpected * 20)];
			++accuracy2[(int) (loseExpected * 20)];
			++accuracy3[(int) ((1-winExpected) * 10) + 10];
			++accuracy3[(int) ((0-loseExpected) * 10) + 10];
			++totalGames;
			accTotal += (1-winExpected);
			if (winExpected >= 0.5) {
				++correct;
			}
		}
		return ans;
	}
	/*
	private static double[] calcELO(double winnerELO, double loserELO, double winnerRD, double loserRD, double winnerVol, double loserVol, int winnerTime, int loserTime) {
		winnerELO = (winnerELO - startELO)/173.7178;
		loserELO = (loserELO - startELO)/173.7178;
		winnerRD = winnerRD/173.7178;
		loserRD = loserRD/173.7178;
		
		
		//double v = 
		double[] ans = new double[3];
		double loseExpected = 1.0 / (1 + Math.pow(10, (winnerELO - loserELO)/400));
		double winExpected = 1.0 - loseExpected;
		ans[2] = winExpected;
		//double winK = Math.max((256.0 / (winnerGames + 1)), 32.0);
		//double winK = 96;
		//double winNew = (winnerELO + winK * (1 - winExpected));
		//ans[0] = winNew;
		//double loseK = Math.max((256.0 / (loserGames + 1)), 32.0);
		//double loseK = 96;
		//double loseNew = (loserELO + loseK * (0 - loseExpected));
		//ans[1] = loseNew;
		
		++accuracy[(int) (winExpected * 20)];
		++accuracy2[(int) (winExpected * 20)];
		++accuracy2[(int) (loseExpected * 20)];
		//++accuracy3[(int) ((1-winExpected) * 20)];
		//++accuracy3[(int) ((0-loseExpected) * 10) + 10];
		++totalGames;
		accTotal += (1-winExpected);
		return ans;
	}*/
	
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

    	    JsonArray matches = (JsonArray) jsonParser.parse(inline);
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

    	    	    player1 = (JsonObject) ((JsonObject) jsonParser.parse(inline)).get("participant");
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
    	    	    player2 = (JsonObject) ((JsonObject) jsonParser.parse(inline)).get("participant");
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

    	    		simulateSet(newname1, newname2, scores.get(i)[0], scores.get(i)[1], false, online);
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

    	    JsonArray matches = (JsonArray) jsonParser.parse(inline);
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

    	    	    player1 = (JsonObject) ((JsonObject) jsonParser.parse(inline)).get("participant");
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
    	    	    player2 = (JsonObject) ((JsonObject) jsonParser.parse(inline)).get("participant");
    	    	}
    	    	String[] temp = {player1.get("name").getAsString().toLowerCase(), (String)player2.get("name").getAsString().toLowerCase()};
    	    	names.set(i, temp);
    	    }
    	    
    	    String msg = null;
    	    for(int i = 0; i < scores.size(); ++i) {
    	    	try {
    	    		String name1 = names.get(i)[0];
    	    		String name2 = names.get(i)[1];
    	    		simulateSet(new StringBuilder(name1), new StringBuilder(name2), scores.get(i)[0], scores.get(i)[1], false, online);
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
}
