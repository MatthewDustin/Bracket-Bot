import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
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
	private static Object discordObj;
    private static JsonObject discordTree;
    private static FileWriter file;
    public static String playerPath = "./Database/Players.json";
	public static String discordPath = "./Database/DiscordPlayers.json";
    private static Gson gson = new Gson();
    public static String tiers = "SABCF";
    private static String startTier = "D";
    static final int startELO = 1000;
    static final int maxELO = 9999;
    static final double startRD = 350;
    static final double startVol = 0.06;

	public static void main(String[] args) throws Exception {
		setAllTiers("boone");
	}

    private static void closeFW() {
    	try {
			file = new FileWriter(playerPath);
    		file.write(playerTree.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	private static void closeDiscordFW() {
    	try {
			file = new FileWriter(discordPath);
    		file.write(discordTree.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private static void getJson() {
        try {
    		playerObj = jsonParser.parse(new FileReader(playerPath));
    		playerTree = ((JsonElement) playerObj).getAsJsonObject();
			discordObj = jsonParser.parse(new FileReader(discordPath));
			discordTree = ((JsonElement) discordObj).getAsJsonObject();
    	}
    	catch(JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void remake() throws Exception {
		getJson();
        for( Entry<String, JsonElement> item : playerTree.entrySet()) {
            String name = item.getKey();
            JsonElement player = item.getValue();
            if(player.isJsonObject()) {
                JsonObject p = player.getAsJsonObject();
                remakePlayer(p);
		        playerTree.add(name, p);
                
            }
        }
		closeFW();
    }

    public static void simulateSet(StringBuilder sbName1, StringBuilder sbName2, int score1, int score2, boolean online, LocalDate time) throws Exception {
		if (playerObj == null) getJson();
		JsonObject playerObj1 = getPlayer(sbName1);
		JsonObject playerObj2 = getPlayer(sbName2);
		String name1 = sbName1.toString();
		String name2 = sbName2.toString();
		if (playerObj1 == null)
		{
			 throw new Exception(name1);
		}
		if (playerObj2 == null){
			 throw new Exception(name2);
		}
		name1 = sbName1.toString();
		name2 = sbName2.toString();
		
		/* if((name1.equals("rock") || name2.equals("rock")) && time.isAfter(LocalDate.parse("2023-04-01"))) {
			System.out.println("Rock");
		} */
		if (score1 < 0 || score2 < 0 || (score1 == 0 && score2 == 0) || (score1 + score2 > 5)) throw new Exception("Scores");
		if (name1.equals(name2))
		    throw new Exception("duplicate");
		
		if (time.isAfter(PlayerInfo.seasonStart)) {
			JsonObject game = new JsonObject();
			game.addProperty("opponent", name2);
			game.addProperty("score", score1);
			game.addProperty("total", score1 + score2);
			JsonArray games = playerObj1.getAsJsonArray("history");
			games.add(game);
			playerObj1.add("history", games);

			game = new JsonObject();
			game.addProperty("opponent", name1);
			game.addProperty("score", score2);
			game.addProperty("total", score1 + score2);
			games = playerObj2.getAsJsonArray("history");
			games.add(game);
			playerObj2.add("history", games);
		}

		if (score1 > score2) {
			simulateGame(name1, name2, online);
			score1 = 1;
		}
		else {
			simulateGame(name2, name1, online);
			score1 = 0;
		}
		score2 = 1 - score1;
		JsonArray times = playerObj1.getAsJsonArray("times");
		JsonArray elos = playerObj1.getAsJsonArray("elos");
		JsonArray stateElos = playerObj1.getAsJsonArray("state elos");
		times.add(time.toString());
		elos.add(playerObj1.get("local ELO").getAsInt());
		stateElos.add(playerObj1.get("state ELO").getAsInt());
		playerObj1.add("times", times);
		times = playerObj2.getAsJsonArray("times");
		elos = playerObj2.getAsJsonArray("elos");
		stateElos = playerObj2.getAsJsonArray("state elos");
		times.add(time.toString());
		elos.add(playerObj2.get("local ELO").getAsInt());
		stateElos.add(playerObj2.get("state ELO").getAsInt());
		playerObj2.add("times", times);

		JsonObject matchup;
		JsonObject headToHead = playerObj1.getAsJsonObject("head to head");
		if ((matchup = (JsonObject) headToHead.get(name2)) == null) {
			matchup = new JsonObject();
		} else {
			score1 += matchup.get("W").getAsInt();
			score2 += matchup.get("L").getAsInt();
		}
		matchup.addProperty("W", score1);
		matchup.addProperty("L", score2);
		headToHead.add(name2, matchup);
		playerObj1.add("head to head", headToHead);

		JsonObject matchup2 = new JsonObject();
		headToHead = playerObj2.getAsJsonObject("head to head");
		matchup2.addProperty("W", score2);
		matchup2.addProperty("L", score1);
		headToHead.add(name1, matchup2);
		playerObj2.add("head to head", headToHead);

		int player1Sets =  playerObj1.get("sets").getAsInt();
		int player2Sets = playerObj2.get("sets").getAsInt();
		playerObj1.addProperty("sets", player1Sets + 1);
		playerObj2.addProperty("sets", player2Sets + 1);
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
		}
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

    private static double[] calcELO(double winnerELO, double loserELO, int winnerGames, int loserGames, boolean online) {
		double[] ans = new double[3];
		double loseExpected = 1.0 / (1 + Math.pow(10, (winnerELO - loserELO)/400));
		double winExpected = 1.0 - loseExpected;
		ans[2] = winExpected;
		double winK = Math.max((150.0 / (winnerGames + 1)), 36.0);
		/* if(winExpected < 0.2){
			System.out.println();
		} */
		double loseK = Math.max((150.0 / (loserGames + 1)), 36.0);
		if (online) {
			winK *= 0.55;
			loseK *= 0.55;
		}
		double winNew = (winnerELO + winK * (1 - winExpected));
		ans[0] = winNew;
		double loseNew = (loserELO + loseK * (0 - loseExpected));
		ans[1] = loseNew;
		
		return ans;
	}

	

	public static ArrayList<Set<String>> getAllTiers(String town) {
		ArrayList<Set<String>> tierList = new ArrayList<Set<String>>(5);
		for(Entry<String, JsonElement> item : playerTree.entrySet()) {
			JsonElement p = item.getValue();
			if(p.isJsonObject()) {
				JsonObject player = p.getAsJsonObject();
				if(player.get("town").getAsString().equals(town)) {
					String name = item.getKey();
					String tier = player.get("local tier").getAsString();
					tierList.get(tiers.indexOf(tier)).add(name);
				}
			}
		}
		return tierList;
	}

	public static ArrayList<Set<String>> getAllTiers() {
		ArrayList<Set<String>> tierList = new ArrayList<Set<String>>(5);
		for(Entry<String, JsonElement> item : playerTree.entrySet()) {
			JsonElement p = item.getValue();
			if(p.isJsonObject()) {
				JsonObject player = p.getAsJsonObject();
				String name = item.getKey();
				String tier = player.get("state tier").getAsString();
				tierList.get(tiers.indexOf(tier)).add(name);
			}
		}
		return tierList;
	}

	public static void setAllTiers(String town) {
		getJson();
		ArrayList<String> competitorsList = new ArrayList<>();
		for(Entry<String, JsonElement> item : playerTree.entrySet()) {
			JsonElement p = item.getValue();
			if(p.isJsonObject()) {
				if(p.getAsJsonObject().get("town").getAsString().equals(town)) {
					competitorsList.add(item.getKey());
				}
			}
		}
		
		for (int i = 0; i < 5 && !competitorsList.isEmpty(); ++i) {
			Set<String> tier = getSmithSet(competitorsList);
			competitorsList.removeIf(n -> (tier.contains(n)));
			for (String p : tier) {
				JsonObject player = playerTree.getAsJsonObject(p);
				player.addProperty("local tier", tiers.charAt(i));
			}
		}
	}

	public static void setAllTiers() {
		getJson();
		ArrayList<String> competitorsList = new ArrayList<>();
		for(Entry<String, JsonElement> item : playerTree.entrySet()) {
			JsonElement p = item.getValue();
			if(p.isJsonObject()) {
				competitorsList.add(item.getKey());
			}
		}
		for (int i = 0; i < 5 && !competitorsList.isEmpty(); ++i) {
			Set<String> tier = getSmithSet(competitorsList);
			competitorsList.removeIf(n -> (tier.contains(n)));
			for (String p : tier) {
				JsonObject player = playerTree.getAsJsonObject(p);
				player.addProperty("state tier", tiers.charAt(i));
			}
		}
	}

	public static Set<String> getSmithSet(ArrayList<String> competitorsList ) {
		int n = competitorsList.size();
	
		// Initialize the graph
		boolean[][] graph = new boolean[n][n];
		for( int i = 0; i < n; i++) {
			JsonObject competitor = playerTree.getAsJsonObject(competitorsList.get(i));
			Set<Entry<String, JsonElement>> headToHeadObject = competitor.getAsJsonObject("head to head").entrySet();
			if(headToHeadObject.isEmpty()) {
				Arrays.fill(graph[i], true);
			}
			for (Entry<String, JsonElement> headToHeads : headToHeadObject) {
				JsonObject h2h = headToHeads.getValue().getAsJsonObject();
				int j = competitorsList.indexOf(headToHeads.getKey());
				if(j > -1) {
					int w = h2h.get("W").getAsInt();
					int l = h2h.get("L").getAsInt();
					if(l > w) {
						graph[i][j] = true;
					}
				}
			}
		}
	
		// Floyd–Warshall algorithm
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					graph[i][j] = graph[i][j] || (graph[i][k] && graph[k][j]);
				}
			}
		}
	
		// Find the nodes that are not reachable from any other node
		ArrayList<HashSet<String>> smithCandidates = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			HashSet<String> candidateList = new HashSet<>();
			candidateList.add(competitorsList.get(i));
			for (int j = 0; j < n; j++) {
				if (graph[i][j]) {
					candidateList.add(competitorsList.get(j));
				}
			}
			smithCandidates.add(candidateList);
		}
	
		return smithCandidates.stream().min(Comparator.comparingInt(Set::size)).get();
	}

	public static int[] getRecord(String name1, String name2) throws Exception {
    	getJson();
    	StringBuilder newname1 = new StringBuilder(name1);
		StringBuilder newname2 = new StringBuilder(name2);
		JsonObject obj1 = (JsonObject) PlayerBuilder.getPlayer(newname1);
		JsonObject obj2 = (JsonObject) PlayerBuilder.getPlayer(newname2);
		name1 = newname1.toString();
		name2 = newname2.toString();
		if (obj1 == null) throw new Exception(name1);
		if (obj2 == null) throw new Exception(name2);
		if (name1.equals(name2)) throw new Exception("duplicate");
		JsonObject matchup = (JsonObject) obj1.get("head to head").getAsJsonObject().get(newname2.toString());
		if (matchup == null) {
			throw new Exception("none");
		} else {
			int[] record = new int[3];
			record[1] = (matchup.get("W").getAsNumber()).intValue();
			record[2] = (matchup.get("L").getAsNumber()).intValue();
			record[0] = record[1] + record[2];
			return record;
		} //TODO: make this return online and offline records
	}

	public static File getPlayerCard(String p) throws Exception {
		JsonObject player = getPlayer(new StringBuilder(p));
		if (player == null)	{ throw new Exception(p); }
		File f = new File(p + "card.png");
		/* if (!f.exists()) */ PlayerInfo.generatePlayerCard(p, player, f);
		return f;
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

	public static String getDiscordPlayer(String ID) {
		if (discordObj == null) getJson();
		JsonElement d = discordTree.get(ID);
		if (d == null) return null;
		return d.getAsString();
	}
	public static void addDiscordPlayer(String ID, String name) {
		if (discordObj == null) getJson();
		discordTree.addProperty(ID, name);
		closeDiscordFW();
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
		/* for(String s : lname) {
			if (playerTree.has(s)) {
				name = s;
			}
		} */
		String strippedname = name.replaceAll("\\p{Zs}", "");
		if (playerTree.has(strippedname)) {
			name = strippedname;
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
		if (playerObj == null | playerTree == null) getJson();
		if (playerTree.has(name)) throw new Exception(name);
		
		for(String s : aliases) {
			if (playerTree.has(s)) throw new Exception(s);
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

	public static void addAlias(StringBuilder name, String[] aliases) throws Exception {
		if (getPlayer(name) == null) throw new Exception("name");
		for(String s : aliases) {
			if (playerTree.has(s)) throw new Exception(s);
		}
		for(String s : aliases) {
			System.out.println(name.toString() + " " + s);
			playerTree.addProperty(s.toLowerCase(), name.toString());
		}
		closeFW();
	}

	/*public static String getAliases(StringBuilder name) throws Exception {
    	if (playerObj == null) getJson();
		if (getPlayer(name) == null) throw new Exception("name");
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
    }*/

	public static void remakePlayer(JsonObject newPlayer) {
		
		//newPlayer.addProperty("name", name);
		newPlayer.addProperty("local ELO", startELO);
		newPlayer.addProperty("state ELO", startELO);
        newPlayer.addProperty("local tier", startTier);
		newPlayer.addProperty("state tier", startTier);
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
		JsonObject headToHead = new JsonObject();
        JsonObject bestWin = new JsonObject();
		newPlayer.add("head to head", headToHead);
        newPlayer.add("history", matchHistory);
        newPlayer.add("bestWin", bestWin);
		
	}

	public static double hotness(StringBuilder player1, StringBuilder player2) throws Exception {
    	if (playerObj == null) getJson();
		JsonObject obj1 = (JsonObject) getPlayer(player1);
		JsonObject obj2 = (JsonObject) getPlayer(player2);
		String newname1 = player1.toString();
		String newname2 = player2.toString();
		if (obj1 == null) throw new Exception(newname1);
		if (obj2 == null) throw new Exception(newname2);
		if (player1.equals(player2)) throw new Exception("duplicate");
		
		double winnerELO = obj1.get("ELO").getAsDouble();
		double loserELO = obj2.get("ELO").getAsDouble();
		int winnerGames = (obj1.get("games").getAsNumber()).intValue();
		int loserGames = (obj2.get("games").getAsNumber()).intValue();
		double eloOdds = calcELO(winnerELO, loserELO, winnerGames, loserGames, false)[2];
		JsonObject matchup = (JsonObject) obj1.get(newname2);
		if (matchup != null) {
			int w = (matchup.get("W").getAsNumber()).intValue();
			double total = w + matchup.get("L").getAsDouble();
			double realOdds = (w) / total;
			System.out.print(realOdds + " ");
			return 10 * (eloOdds + realOdds ) / 2; 
		} 
		return 10 * eloOdds;
    }

	

    /*public static int[][] getHistory(JsonObject p) {
        JsonArray elos = p.get("elos").getAsJsonArray();
		JsonArray times = p.get("times").getAsJsonArray();
		int[][] ans = new int[2][100];
		for(int i = 0; i < 100; ++i){
			ans[0][i] = elos.get(i).getAsInt();
			ans[1][i] = times.get(i).getAsInt();
		}
		return ans;
    }*/
}
