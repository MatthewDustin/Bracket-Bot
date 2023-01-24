import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
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


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;

public class Slippi {
    public static void main(String[] args) {
        try {
            getSlippi();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void getSlippi() throws IOException {
		URL url = new URL("https://gql-gateway-dot-slippi.uc.r.appspot.com/graphql");
    	
    	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    	conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        String query = "fragment userProfilePage on User {" +
            "displayName" +
            "connectCode {" +
                  "code" +
                  "__typename" +
                "}" +
              "rankedNetplayProfile {" +
                    "id" +
                    "ratingOrdinal" +
                    "ratingUpdateCount" +
                    "wins" +
                    "losses" +
                    "dailyGlobalPlacement" +
                    "dailyRegionalPlacement" +
                    "continent" +
                    "characters {" +
                            "id" +
                            "character" +
                            "gameCount" +
                            "__typename" +
                          "}" +
                    "__typename" +
                  "}" +
              "__typename" +
          "}" +
          "query AccountManagementPageQuery($cc: String!) {" +
              "getConnectCode(code: $cc) {" +
                    "user {" +
                            "...userProfilePage" +
                            "__typename" +
                          "}" +
                    "__typename" +
                  "}" +
          "}";
        String connectCode = "Mag#990";
        String requestString = "{\"operationName\": \"AccountManagementPageQuery\", " + query + ", variables: { cc: " + connectCode + " },}";
    	try {
            OutputStream os = conn.getOutputStream();
            byte[] input = requestString.getBytes("utf-8");
            os.write(input, 0, input.length);			
        } finally {

        }

    	int responsecode = conn.getResponseCode();
    	
    	if (responsecode != 200) {
    	    throw new RuntimeException("HttpResponseCode: " + responsecode);
    	} else {
    		String inline = "";
    	    Scanner scanner = new Scanner(url.openStream());
    	  
    	    while (scanner.hasNext()) {
    	       inline += scanner.nextLine();
    	    }
    	    
    	    scanner.close();
            System.out.println(inline);
        }
    }
}
