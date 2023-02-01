import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Startgg {
    public static void main(String[] args) {
        try {
            
            getStartgg("tournament/melee-mondays-weekly-1-picantetcg/event/melee-singles", false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getStartgg(String slug, boolean online) throws Exception {
      JsonParser jsonParser = new JsonParser();
		  OkHttpClient client = new OkHttpClient().newBuilder()
        .build();
      String pages = "1";
      String perPage = "100";
      MediaType mediaType = MediaType.parse("application/json");
      String json = "{\"query\":\"query EventSets($eventSlug: String!, $page: Int!, $perPage: Int!) {\\r\\n  event(slug: $eventSlug) {\\r\\n    id\\r\\n    name\\r\\n    sets(\\r\\n      page: $page\\r\\n      perPage: $perPage\\r\\n      sortType: STANDARD\\r\\n    ) {\\r\\n      pageInfo {\\r\\n        total\\r\\n      }\\r\\n      nodes {\\r\\n        id\\r\\n        winnerId\\r\\n        displayScore\\r\\n        totalGames\\r\\n        slots {\\r\\n          id\\r\\n          entrant {\\r\\n            id\\r\\n            name\\r\\n          }\\r\\n        }\\r\\n      }\\r\\n    }\\r\\n  }\\r\\n},\",\"variables\":{\"eventSlug\":\""+ slug + "\",\"page\":" + pages + ",\"perPage\":"+ perPage +"}}";
      RequestBody body = RequestBody.create(mediaType, json);
      Request request = new Request.Builder()
        .url("https://api.smash.gg/gql/alpha")
        .method("POST", body)
        .addHeader("Authorization", "Bearer " + JsonBuilder.apiKeyStart)
        .addHeader("Content-Type", "application/json")
        .build();
      Response response = client.newCall(request).execute();
      String jsonResponse = response.body().string();

      System.out.println(jsonResponse);
      JsonObject event = jsonParser.parse(jsonResponse).getAsJsonObject().get("data").getAsJsonObject().get("event").getAsJsonObject();
    	int id = event.get("id").getAsInt();
      JsonArray sets = event.get("sets").getAsJsonObject().get("nodes").getAsJsonArray();

      ArrayList<String[]> setsCSV = new ArrayList<String[]>();
      String title = slug.split("/")[1];
      setsCSV.add(new String[]{title});
      for(JsonElement set : sets) {
        JsonObject setO = set.getAsJsonObject();
        String winnerID = setO.get("winnerId").getAsString();
        int games = setO.get("totalGames").getAsInt();
        String scoreLine = setO.get("displayScore").getAsString();
        int Lscore = Character.getNumericValue(scoreLine.charAt(scoreLine.length()-1));
        int Wscore = Character.getNumericValue(scoreLine.charAt(scoreLine.indexOf(" - ") - 1));
        
        JsonObject entrant1 = setO.get("slots").getAsJsonArray().get(0).getAsJsonObject().get("entrant").getAsJsonObject();
        JsonObject entrant2 = setO.get("slots").getAsJsonArray().get(1).getAsJsonObject().get("entrant").getAsJsonObject();
        int id1 = entrant1.get("id").getAsInt();
        int id2 = entrant2.get("id").getAsInt();
        StringBuilder sbname1 = new StringBuilder(entrant1.get("name").getAsString());
        StringBuilder sbname2 = new StringBuilder(entrant2.get("name").getAsString());
        JsonBuilder.getPlayer(sbname1);
        JsonBuilder.getPlayer(sbname2);
        if (Lscore > Wscore) {
          int t = Lscore;
          Lscore = Wscore;
          Wscore = t;
          StringBuilder tempsb = sbname1;
          sbname1 = sbname2;
          sbname2 = tempsb;
        }
        //JsonBuilder.simulateSet(sbname1, sbname2, games - score, score, false, online);
        
        setsCSV.add(new String[] {sbname1.toString(), sbname2.toString(), Integer.toString(Wscore), Integer.toString(Lscore)});
      }
      CSVWriter csvWriter = new CSVWriter(new FileWriter(title + ".csv"));
      csvWriter.writeAll(setsCSV);
      csvWriter.close();
      return title;
    }
    
	
}
