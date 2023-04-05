import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class PlayerBuilder {
    private static JsonParser jsonParser = new JsonParser();
    private static Object playerObj;
    private static JsonObject playerTree;
    private static FileWriter file;
    private static String playerPath = "./Database/Players.json";
    private static Gson gson = new Gson();
    
    private static String startTier = "D";
    static final int startELO = 1000;
    static final int maxELO = 9999;
    static final double startRD = 350;
    static final double startVol = 0.06;

    private static void closeFW() {
    	try {
			file = new FileWriter(playerPath);
    		file.write(playerTree.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    private static void getJson() {
        try {
    		playerObj = jsonParser.parse(new FileReader(playerPath));
    		playerTree = ((JsonElement) playerObj).getAsJsonObject();
    	}
    	catch(JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void remake() throws Exception {
        for( Entry<String, JsonElement> item : playerTree.entrySet()) {
            String name = item.getKey();
            JsonElement player = playerTree.get(name);
            if(player.isJsonObject()) {
                JsonObject p = player.getAsJsonObject();
                remakePlayer(p);
		        playerTree.add(name, p);
                
            }
        }
		closeFW();
    }

    public static void simulateSet(StringBuilder sbName1, StringBuilder sbName2, int score1, int score2, boolean online, boolean thisSeason, Date time) throws Exception {
		getJson();
		String name1 = sbName1.toString();
		String name2 = sbName2.toString();
		
		JsonObject playerObj1 = getPlayer(sbName1);
		JsonObject playerObj2 = getPlayer(sbName2);
		if (playerObj1 == null)
		{
			 throw new Exception(name1);
		}
		if (playerObj2 == null){
			 throw new Exception(name2);
		}
		name1 = sbName1.toString();
		name2 = sbName2.toString();
		
		if (score1 < 0 || score2 < 0 || (score1 == 0 && score2 == 0) || (score1 + score2 > 5)) throw new Exception("Scores");
		if (name1.equals(name2))
		    throw new Exception("duplicate");
		for(int i = 0; i < score1; ++i) {
			simulateGame(name1, name2, online);
		}
		for(int i = 0; i < score2; ++i) {
			simulateGame(name2, name1, online);
		}
		
		JsonArray times = playerObj1.getAsJsonArray("times");
		JsonArray elos = playerObj1.getAsJsonArray("elos");
		JsonArray stateElos = playerObj1.getAsJsonArray("state elos");
		times.add(time.getTime());
		elos.add(playerObj1.get("local ELO").getAsInt());
		stateElos.add(playerObj1.get("state ELO").getAsInt());
		playerObj1.add("times", times);
		
		times = playerObj2.getAsJsonArray("times");
		elos = playerObj2.getAsJsonArray("elos");
		stateElos = playerObj2.getAsJsonArray("state elos");
		times.add(time.getTime());
		elos.add(playerObj2.get("local ELO").getAsInt());
		stateElos.add(playerObj2.get("state ELO").getAsInt());
		playerObj2.add("times", times);

		JsonObject matchup;
		JsonObject matchup2;
		if ((matchup = (JsonObject) playerObj1.get(name2)) == null) {
			matchup = new JsonObject();
			matchup.addProperty("W", 1);
			matchup.addProperty("L", 0);
		} else {
			int w = (matchup.get("W").getAsNumber()).intValue();
			int l = (matchup.get("L").getAsNumber()).intValue();
			matchup.addProperty("W", w + score1);
			matchup.addProperty("L", l + score2);
		}
		if ((matchup2 = (JsonObject) playerObj2.get(name1)) == null) {
			matchup2 = new JsonObject();
			matchup2.addProperty("W", score2);
			matchup2.addProperty("L", score1);
		} else {
			int w = (matchup2.get("W").getAsNumber()).intValue();
			int l = (matchup2.get("L").getAsNumber()).intValue();
			matchup2.addProperty("W", w + score2);
			matchup2.addProperty("L", l + score1);
		}
		int player1Sets =  playerObj1.get("sets").getAsInt();
		int player2Sets = playerObj2.get("sets").getAsInt();
		playerObj1.addProperty("sets", player1Sets + 1);
		playerObj2.addProperty("sets", player2Sets + 1);

		playerObj1.add(name2, matchup);
		playerObj2.add(name1, matchup2);
		playerTree.add(name1, playerObj1);
		playerTree.add(name2, playerObj2);
		
		closeFW();
	}

    private static void simulateGame(String name1, String name2, boolean online) {
		JsonObject winner = (JsonObject) playerTree.get(name1);
		JsonObject loser = (JsonObject) playerTree.get(name2);
		
		String winnerTown = winner.get("town").getAsString();
		String loserTown = loser.get("town").getAsString();
		boolean local = winnerTown.equals(loserTown);
		double winnerELO;
		double loserELO;
		
		
		int winnerSets =  winner.get("sets").getAsInt();
		int loserSets = loser.get("sets").getAsInt();
		double winnerRD =  winner.get("ELORD").getAsDouble();
		double winnerVol = winner.get("ELOVol").getAsDouble();
		double loserRD = loser.get("ELORD").getAsDouble();
		double loserVol =  loser.get("ELOVol").getAsDouble();
		//double[] newELOs = calcELO(winnerELO, loserELO, winnerRD, loserRD, winnerVol, loserVol, winnerTime, loserTime);
		double[] newELOs;
		if(local) {
			winnerELO = winner.get("local ELO").getAsDouble();
			loserELO =  loser.get("local ELO").getAsDouble();
			newELOs = calcELO(winnerELO, loserELO, winnerSets, loserSets, online);
			if(loserSets > 3 || winnerSets <= 3) {
				winner.addProperty("local ELO", newELOs[0]);
				playerTree.add(name1, winner);
			}
			if(winnerSets > 3 || loserSets <= 3) {
				loser.addProperty("local ELO", newELOs[1]);
				playerTree.add(name2, loser);
			}
		} else {
			winnerELO = winner.get("state ELO").getAsDouble();
			loserELO =  loser.get("state ELO").getAsDouble();
			newELOs = calcELO(winnerELO, loserELO, winnerSets, loserSets, online);
			if(loserSets > 3 || winnerSets <= 3) {
				winner.addProperty("state ELO", newELOs[0]);
				playerTree.add(name1, winner);
			}
			if(winnerSets > 3 || loserSets <= 3) {
				loser.addProperty("state ELO", newELOs[1]);
				playerTree.add(name2, loser);
			}
		}
		
	}

    private static double[] calcELO(double winnerELO, double loserELO, int winnerGames, int loserGames, boolean online) {
		double[] ans = new double[3];
		double loseExpected = 1.0 / (1 + Math.pow(10, (winnerELO - loserELO)/400));
		double winExpected = 1.0 - loseExpected;
		ans[2] = winExpected;
		double winK = Math.max((150.0 / (winnerGames + 1)), 36.0);
		
		double loseK = Math.max((150.0 / (loserGames + 1)), 36.0);
		if (online) {
			winK *= 0.75;
			loseK *= 0.75;
		}
		double winNew = (winnerELO + winK * (1 - winExpected));
		ans[0] = winNew;
		double loseNew = (loserELO + loseK * (0 - loseExpected));
		ans[1] = loseNew;
		
		return ans;
	}

	/*
     * returns false if changes failed
     */
    public static boolean changePlayer(String name, String key, Object value) {
    	if (playerObj == null) getJson();
    	StringBuilder n = new StringBuilder(name);
    	JsonObject player = getPlayer(n);
    	name = n.toString();
    	if (player == null) return false;
		JsonElement val = jsonParser.parse(value.toString());
		player.add(key, val);
    	playerTree.add(name, player);
    	closeFW();
    	return true;
    }

    public static boolean increaseAttendance(String name) {
        if (playerObj == null) getJson();
    	StringBuilder n = new StringBuilder(name);
    	JsonObject player = getPlayer(n);
    	name = n.toString();
        if (player == null) return false;
        int a = player.get("attendance").getAsInt();
        player.addProperty("attendance", a + 1);
        closeFW();
        return true;
    }

    public static JsonObject getPlayer(StringBuilder sb) {
		if (playerObj == null) getJson();
		
    	String name = sb.toString().toLowerCase();
		int team = name.indexOf(" | ") + 3;
		if (team > 3) {
			name = name.substring(team);
		}
		List<String> lname = Arrays.asList(name.split(" "));
		Collections.reverse(lname);
		for(String s : lname) {
			if (playerTree.has(s)) {
				name = s;
			}
		}
    	if (!playerTree.has(name)) {
    		String xname = name.replaceAll("\\p{Punct}", "");
    		if (playerTree.has(xname)) {
    			name = xname;
    		} else {
				lname = Arrays.asList(xname.split(" "));
				Collections.reverse(lname);
				for(String s : lname) {
					if (playerTree.has(s)) {
						name = s;
					}
				}
				String sname = name.replaceAll("\\p{Punct}", " ");
				lname = Arrays.asList(sname.split(" "));
				Collections.reverse(lname);
				for(String s : lname) {
					if (playerTree.has(s)) {
						name = s;
					}
				}
    		}
		}
    	JsonElement player = playerTree.get(name);

		if (player == null) return null;

    	if (player.isJsonPrimitive()) {
    		sb.replace(0, sb.length(), player.getAsString());
    		player = playerTree.get( player.getAsString());
    	} 
    	else {
    		sb.replace(0, sb.length(), name); 
		}
    	return (JsonObject) player;
    }
    /*
     * returns false if changes failed
     */
    public static boolean deletePlayer(String name) {
    	if (playerObj == null) getJson();
    	if (!playerTree.has(name)) return false;
    	playerTree.remove(name);
    	closeFW();
    	return true;
    }

    /*
     * 
     */
	public static void addPlayer(String name, String town, String[] aliases, String[] mains) throws Exception {
		if (playerObj == null | playerObj == null) getJson();
		if (playerTree.has(name)) throw new Exception(name);
		
		for(String s : aliases) {
			if (playerTree.has(s)) throw new Exception(s);
			if (s.equals("") || s.equals(" ")){
				System.out.println();
			}
			playerTree.addProperty(s.toLowerCase(), name);
		}
		
		JsonObject newPlayer = new JsonObject();
		newPlayer.addProperty("town", town);
		JsonArray m = jsonParser.parse(gson.toJson(mains)).getAsJsonArray();
        newPlayer.add("mains", m);
		remakePlayer(newPlayer);
		playerTree.add(name, newPlayer);
		closeFW();
		/*getJson();
		if (remake) return;
		
		FileWriter playerWrite = new FileWriter(playerPath, true);
		playerWrite.append("\n" + name + " " + local + " ");
		
		if(aliases.length > 0) {
			playerWrite.append(aliases[0]);
		}
		playerWrite.close();*/
	}

	public static void remakePlayer(JsonObject newPlayer) {
		
		//newPlayer.addProperty("name", name);
		newPlayer.addProperty("local ELO", startELO);
		newPlayer.addProperty("state ELO", startELO);
        newPlayer.addProperty("tier", startTier);
		newPlayer.addProperty("sets", 0);
		
		newPlayer.addProperty("ELORD", startRD);
		newPlayer.addProperty("ELOVol", startVol);
		newPlayer.addProperty("attendance", 0);
		newPlayer.addProperty("lastTourney", "");
        
		JsonArray elos = new JsonArray();
		JsonArray stateElos = new JsonArray();
		JsonArray times = new JsonArray();
        
		newPlayer.add("elos", elos);
		newPlayer.add("state elos", stateElos);
		newPlayer.add("times", times);
		
        JsonArray matchHistory = new JsonArray();
        JsonObject bestWin = new JsonObject();
        newPlayer.add("history", matchHistory);
        newPlayer.add("bestWin", bestWin);
		
	}


    public static int[][] getHistory(JsonObject p) {
        JsonArray elos = p.get("elos").getAsJsonArray();
		JsonArray times = p.get("times").getAsJsonArray();
		int[][] ans = new int[2][100];
		for(int i = 0; i < 100; ++i){
			ans[0][i] = elos.get(i).getAsInt();
			ans[1][i] = times.get(i).getAsInt();
		}
		return ans;
    }
}
