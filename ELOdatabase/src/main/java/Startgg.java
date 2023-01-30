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

public class Startgg {
    public static void main(String[] args) {
        try {
            getStartgg();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void getStartgg() throws IOException {
		  OkHttpClient client = new OkHttpClient().newBuilder()
        .build();
      MediaType mediaType = MediaType.parse("application/json");
      RequestBody body = RequestBody.create(mediaType, "{\"query\":\"query EventSets($eventSlug: String!, $page: Int!, $perPage: Int!) {\\r\\n  event(slug: $eventSlug) {\\r\\n    id\\r\\n    name\\r\\n    sets(\\r\\n      page: $page\\r\\n      perPage: $perPage\\r\\n      sortType: STANDARD\\r\\n    ) {\\r\\n      pageInfo {\\r\\n        total\\r\\n      }\\r\\n      nodes {\\r\\n        id\\r\\n        slots {\\r\\n          id\\r\\n          entrant {\\r\\n            id\\r\\n            name\\r\\n          }\\r\\n        }\\r\\n      }\\r\\n    }\\r\\n  }\\r\\n},\",\"variables\":{\"eventSlug\":\"tournament/melee-mondays-weekly-1-picantetcg/event/melee-singles\",\"page\":1,\"perPage\":100}}");
      Request request = new Request.Builder()
        .url("https://api.smash.gg/gql/alpha")
        .method("POST", body)
        .addHeader("Authorization", "Bearer 220406d16d278620593e1af51a6201bb")
        .addHeader("Content-Type", "application/json")
        .build();
      Response response = client.newCall(request).execute();
      String jsonResponse = response.body().string();

      System.out.println(jsonResponse);
    }
}