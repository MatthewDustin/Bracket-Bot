import javax.security.auth.login.LoginException;
import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class BotStartup {
	public static JDA client;
	public static void main(String[] args) throws LoginException {
		JDABuilder jda = JDABuilder.createDefault("OTQ3MDE5ODgwOTE2NDgwMDMw.GTQWh9.XPyGaVynrVEZCvf_Jm0yDecNICFH5ZAVr7MCl8");
		jda.setActivity(Activity.watching("/help"));
		jda.setStatus(OnlineStatus.ONLINE);
		jda.addEventListeners(/* new Commands(), */ new CommandManager());
		client = jda.build();

		JsonBuilder.getJson();
	}
	
	public static void shutdown() {
		System.out.println("CLIENT SHUTTING DOWN");
		client.shutdown();
	}
	
	public static void restart() throws LoginException {
		System.out.println("CLIENT RESTARTING");
		client.shutdown();
		main(null);
	}
}
