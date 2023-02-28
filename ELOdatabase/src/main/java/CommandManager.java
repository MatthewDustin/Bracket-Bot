import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

public class CommandManager extends ListenerAdapter{
 
    private List<String> doneGames = new ArrayList<String>();
	private Timer timer;
    public final long magID = 183728250822983680L;

    @Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		String command = event.getName();
		String usertag = event.getUser().getAsTag();

        System.out.println(event.isFromGuild());
        if(command.equalsIgnoreCase("addtourney")) {
            try {
                OptionMapping msgOption = event.getOption("link");
                OptionMapping online = event.getOption("online");
                String slugLink = msgOption.getAsString();
                int begin = slugLink.indexOf("t.gg") + 5;
                slugLink = slugLink.substring(begin);
                String[] temp = slugLink.split("/");
                slugLink = temp[0] + "/" + temp[1] + "/" + temp[2] + "/" + temp[3];
                System.out.println(slugLink);
                String title = Startgg.getStartgg(slugLink, online.getAsBoolean());
                File file = new File(title + ".csv");
                FileUpload upload = FileUpload.fromData(file);
                event.reply(title + "...finished").addFiles(upload).queue();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (command.equalsIgnoreCase("showrankings")) {
			event.reply(JsonBuilder.getRankings()).queue();
		}

        if (command.equalsIgnoreCase("record")) {
			try {
                String player1 = event.getOption("player1").getAsString();
                String player2 = event.getOption("player2").getAsString();
				int[] record = JsonBuilder.getRecord(player1, player2);
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(Color.red);
				eb.setTitle(player1 + " vs. " + player2, null);
				eb.addField("Games", String.valueOf(record[0]), true);
				eb.addField(player1 + " dubs", String.valueOf(record[1]), true);
				eb.addField(player2 + " dubs", String.valueOf(record[2]), true);
				
                event.replyEmbeds(eb.build()).queue();
			} catch (Exception e) {
				String msg = e.getMessage();
				if (msg == null) {
					e.printStackTrace();
				} else if (msg.equals("none")) {
					event.reply("No matches exist yet.").queue(); 
				}
			}
		}

        if (command.equalsIgnoreCase("aliases")) {
            String player = event.getOption("player").getAsString();
			try {
				String ans = JsonBuilder.getAliases(player);
				event.reply("Aliases: " + ans).queue();
			} catch (Exception e) {
				String message = e.getMessage();
                if (message.equals("none")) {
					event.reply(player + " does not exist").queue();
				}
				else {
					e.printStackTrace();
				}
			}
		}

        if (command.equalsIgnoreCase("hotness")) {
			try {
                String player1 = event.getOption("player1").getAsString();
                String player2 = event.getOption("player2").getAsString();
				double hotness = JsonBuilder.hotness(player1, player2);
				System.out.println(hotness);
				if (hotness > 4 && hotness < 6) {
					event.reply(":fire: **" + player1 + "** vs. **" + player2 + "** is a very Hot Set!:fire:").queue(); 
				} else if (hotness > 3 && hotness < 7) {
					event.reply("**" + player1 + "** vs. **" + player2 + "** is a Hot Set!").queue(); 
				}
				else {
					event.reply("Hotness inconclusive").queue(); 
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

        if (event.getUser().getIdLong() != magID) return;

        System.out.println(event.getUser().getAsTag());
        if(command.equalsIgnoreCase("restart")) {
			try {
				BotStartup.restart();
			} catch (LoginException e) {
				e.printStackTrace();
			}
		}

        if (command.equalsIgnoreCase("remake")) {
			try {
                event.deferReply().queue();
				JsonBuilder.remakeFile();
                event.getHook().sendMessage("Database remade!").queue(); // delayed response updates our inital "thinking..." message with the tag value
			} catch(Exception e) {
				event.reply("Finished with errors (check console)").queue();
				e.printStackTrace();
			}
		}
		if(command.equalsIgnoreCase("shutdown")) {
			event.reply("Goodbye!").queue();
			BotStartup.shutdown();
		}

	}

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        OptionData player1 = new OptionData(OptionType.STRING, "player1", "Player 1", true);
        OptionData player2 = new OptionData(OptionType.STRING, "player2", "Player 2", true);
        OptionData player = new OptionData(OptionType.STRING, "player", "Player Name", true);
        OptionData slugLink = new OptionData(OptionType.STRING, "link", "https://www.start.gg/tournament/melee-mondays-weekly-1-picantetcg/event/melee-singles/", true);
        OptionData online = new OptionData(OptionType.BOOLEAN, "online", "Was the tournament Online?", true);
        commandData.add(Commands.slash("addtourney", "Get a CSV file of sets from a tourney.").addOptions(slugLink, online));
        commandData.add(Commands.slash("record", "Get the head-to-head record of two players.").addOptions(player1, player2));
        commandData.add(Commands.slash("hotness", "How 'HOT' would this set be? Give me two players.").addOptions(player1, player2));
        commandData.add(Commands.slash("aliases", "Get other nicknames of a player.").addOptions(player));
        commandData.add(Commands.slash("showrankings", "Get my Elo rankings for NC players."));
        commandData.add(Commands.slash("restart", "Restart the bot."));
        commandData.add(Commands.slash("remake", "Remake the derived json data."));
        commandData.add(Commands.slash("shutdown", "Shutdown the bot."));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}

/*
 * if (command.equalsIgnoreCase("get")) {
			timer = new Timer();
			
			timer.schedule(new TimerTask() {
				long counter = 0;
				@Override
				public void run() {
					try {
						counter++;
						if (counter >= 600) timer.cancel();
						ArrayList<String[]> matches = JsonBuilder.getUpcomingMatches(args[1]);
						for(String[] s : matches) {
							if (doneGames.contains(s[2])) continue;
							doneGames.add(s[2]);
							System.out.println(s[0] + " " + s[1]);
							double hotness = JsonBuilder.hotness(s[0].toLowerCase(), s[1].toLowerCase());
							
							if (hotness > 4 && hotness < 6) {
								event.reply(":fire: **" + s[0] + "** vs. **" + s[1] + "** is a very Hot Set!:fire:").queue(); 
							} else if (hotness > 3 && hotness < 7) {
								event.reply("**" + s[0] + "** vs. **" + s[1] + "** is a Hot Set!").queue(); 
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 30000);
		}
 */