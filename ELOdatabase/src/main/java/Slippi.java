import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import javax.net.ssl.HttpsURLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    /*HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    	conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);*/
    public static void getSlippi() throws IOException {
	  String url ="https://gql-gateway-dot-slippi.uc.r.appspot.com/graphql";
      String connectCode = "Mag#990";
      String q = "fragment userProfilePage on User {\n" +
          "displayName\n" +
          "connectCode {\n" +
                "code\n" +
                "__typename\n" +
              "}\n" +
            "rankedNetplayProfile {\n" +
                  "id\n" +
                  "ratingOrdinal\n" +
                  "ratingUpdateCount\n" +
                  "wins\n" +
                  "losses\n" +
                  "dailyGlobalPlacement\n" +
                  "dailyRegionalPlacement\n" +
                  "continent\n" +
                  "characters {\n" +
                          "id\n" +
                          "character\n" +
                          "gameCount\n" +
                          "__typename\n" +
                        "}\n" +
                  "__typename\n" +
                "}\n" +
            "__typename\n" +
        "}\n" +
        "query AccountManagementPageQuery($cc: String!) {\n" +
            "getConnectCode(code: $cc) {\n" +
                  "user {\n" +
                          "...userProfilePage\n" +
                          "__typename\n" +
                        "}\n" +
                  "__typename\n" +
                "}\n" +
        "}";
        
        String json = "{\"operationName\": \"AccountManagementPageQuery\", \"query\": \"" + q + "\", \"variables\": { cc: \"" + connectCode + "\"},}";
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        ObjectMapper mapper = new ObjectMapper();
        String query = mapper.writeValueAsString(json);

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-type", "application/json")
            .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();

        System.out.println(jsonResponse);
    }
}
