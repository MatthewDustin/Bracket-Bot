import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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

    public static void getStartgg(String slug, boolean online) throws Exception {
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

      JsonObject event = jsonParser.parse(jsonResponse).getAsJsonObject().get("data").getAsJsonObject().get("event").getAsJsonObject();
    	int id = event.get("id").getAsInt();
      JsonArray sets = event.get("sets").getAsJsonObject().get("Nodes").getAsJsonArray();

      for(JsonElement set : sets) {
        JsonObject setO = set.getAsJsonObject();
        String winnerID = setO.get("winnerId").getAsString();
        int games = setO.get("totalGames").getAsInt();
        String scoreLine = setO.get("displayScore").getAsString();
        int score = Character.getNumericValue(scoreLine.charAt(scoreLine.length()-1));
        JsonObject entrant1 = setO.get("slots").getAsJsonArray().get(0).getAsJsonObject().get("Entrant").getAsJsonObject();
        JsonObject entrant2 = setO.get("slots").getAsJsonArray().get(1).getAsJsonObject().get("Entrant").getAsJsonObject();
        int id1 = entrant1.get("id").getAsInt();
        int id2 = entrant2.get("id").getAsInt();
        String name1 = entrant1.get("name").getAsString();
        String name2 = entrant2.get("name").getAsString();
        JsonBuilder.simulateSet(name1, name2, games - score, score, false, online);
      }
      ArrayList<String[]> sets = new ArrayList<String[]>();
    	    ArrayList<Integer[]> scores = new ArrayList<Integer[]>();
      System.out.println(jsonResponse);
    }
    public String convertToCSV(String[] data) {
    	return Stream.of(data)
      		.map(this::escapeSpecialCharacters)
      		.collect(Collectors.joining(","));
    }
	public void givenDataArray_whenConvertToCSV_thenOutputCreated(String tourneyName) throws IOException {
    		File csvOutputFile = new File(tourneyName);
    		try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
        		dataLines.stream()
          		.map(this::convertToCSV)
          		.forEach(pw::println);
    		}
    		assertTrue(csvOutputFile.exists());
	}
	public String escapeSpecialCharacters(String data) {
    		String escapedData = data.replaceAll("\\R", " ");
    		if (data.contains(",") || data.contains("\"") || data.contains("'")) {
    		    data = data.replace("\"", "\"\"");
    		    escapedData = "\"" + data + "\"";
    		}
    		return escapedData;
	}
	
}
