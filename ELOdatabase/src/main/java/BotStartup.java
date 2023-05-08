import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.security.auth.login.LoginException;
import org.bson.Document;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotStartup {
	public static JDA client;
	private static JsonParser jsonParser = new JsonParser();
	public static void main(String[] args) throws LoginException, JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonObject apiKeys = jsonParser.parse(new FileReader("./Database/APIKeys.json")).getAsJsonObject();
		TourneyBuilder.apiKey = apiKeys.get("challonge").getAsString();
		TourneyBuilder.apiKeyStart = apiKeys.get("start").getAsString();
		JDABuilder jda = JDABuilder.createDefault(apiKeys.get("discord").getAsString());
		jda.setActivity(Activity.watching("/help"));
		jda.setStatus(OnlineStatus.ONLINE);
		jda.addEventListeners(/* new Commands(), */ new CommandManager());
		jda.enableIntents(GatewayIntent.MESSAGE_CONTENT);
		client = jda.build();
		
		JsonBuilder.getJson();
	}
	
	public static void shutdown() {
		System.out.println("CLIENT SHUTTING DOWN");
		client.shutdown();
	}
	
	public static void restart() throws LoginException, JsonIOException, JsonSyntaxException, FileNotFoundException {
		System.out.println("CLIENT RESTARTING");
		client.shutdown();
		main(null);
	}
}
