
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
import java.util.Set;

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
	static Object playerObj;
    static JsonObject jsonTree;
	static JsonObject playerTree;
    static FileWriter file;
	static FileWriter playerFile;
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
    
    
    public static double[] accuracy = new double[20];
    public static double[] accuracy2 = new double[20];
    public static int[] accuracy3 = new int[20];
    public static int totalGames = 0;
    public static double accTotal = 0.0;
    public static int correct;
    
    public static void main(String[] args) {
    	
    	System.out.println("Working Directory = " + System.getProperty("user.dir"));
    	try {
    		remakeFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    
    

    public static String getRankings(String town) {
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
    
    
    
    public static void remakeFile() throws Exception {
		
    	PlayerBuilder.remake();
		TourneyBuilder.getPlayersFromMatches();
		TourneyBuilder.replayMatches();

		System.out.println("\nRemake finished");
    }
    
    
    
    public static void getJson() {
    	try {
    		obj = jsonParser.parse(new FileReader(jsonPath));
			//playerObj = jsonParser.parse(new FileReader(playerPath));
    		jsonTree = ((JsonElement) obj).getAsJsonObject();
			//System.out.println("json tree: " + jsonTree.getAsString());
			//playerTree = (JsonObject)playerObj;
    	}
    	catch(JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void closeFW() {
    	try {
			file = new FileWriter(jsonPath);
    		file.write(jsonTree.toString());
			//playerFile.write(playerTree.toString());
			//playerFile.close();
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
	
	

	

    
}
