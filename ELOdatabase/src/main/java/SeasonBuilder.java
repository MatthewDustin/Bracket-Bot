import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class SeasonBuilder {

    private static JsonParser jsonParser = new JsonParser();
    private static Object seasonObj;
    private static JsonObject seasonTree;
    private static FileWriter file;
    private static String seasonPath = "./Database/Seasons.json";

    public static void addSeason(String name, int day, int month, int year, int dayEnd, int monthEnd,
            int yearEnd) {
		if (seasonObj == null) getJson();
		
		JsonObject newSeason = new JsonObject();
		newSeason.addProperty("name", name);
		newSeason.addProperty("start day", day);
		newSeason.addProperty("start month", month);
		newSeason.addProperty("start year", year);
		newSeason.addProperty("end day", dayEnd);
		newSeason.addProperty("end month", monthEnd);
		newSeason.addProperty("end year", yearEnd);
		seasonTree.add(name, newSeason);
		closeFW();
    }

	public static void changeSeason(String name, String newName, int day, int month, int year, int dayEnd, int monthEnd,
            int yearEnd) {
		if (seasonObj == null) getJson();
		
		JsonObject newSeason = new JsonObject();
		newSeason.addProperty("name", newName);
		newSeason.addProperty("start day", day);
		newSeason.addProperty("start month", month);
		newSeason.addProperty("start year", year);
		newSeason.addProperty("end day", dayEnd);
		newSeason.addProperty("end month", monthEnd);
		newSeason.addProperty("end year", yearEnd);
		if(name.equals(newName)) {
			seasonTree.add(name, newSeason);
		} else {
			seasonTree.remove(name);
			seasonTree.add(newName, newSeason);
		}
		closeFW();
    }

	

    public static Set<String> getSeasons() {
		if (seasonObj == null) getJson();
		return seasonTree.keySet();
	}

	public static int[] getSeason(String name) {
		if (seasonObj == null) getJson();
		JsonObject season = seasonTree.get(name).getAsJsonObject();
		int[] ans = new int[6];
		ans[0] = season.get("start day").getAsInt();
		ans[1] = season.get("start month").getAsInt();
		ans[2] = season.get("start year").getAsInt();
		ans[3] = season.get("end day").getAsInt();
		ans[4] = season.get("end month").getAsInt();
		ans[5] = season.get("end year").getAsInt();
		return ans;
	}

    private static void closeFW() {
    	try {
			file = new FileWriter(seasonPath);
    		file.write(seasonTree.toString());
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    private static void getJson() {
        try {
    		seasonObj = jsonParser.parse(new FileReader(seasonPath));
    		seasonTree = ((JsonElement) seasonObj).getAsJsonObject();
    	}
    	catch(JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
