import java.io.File;
import java.security.cert.PKIXRevocationChecker.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;

public class CommandManager extends ListenerAdapter{
 
    private List<String> doneGames = new ArrayList<String>();
	private Timer timer;
    public final long magID = 183728250822983680L;
	public static long newDiscordUserID = 0;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message em = event.getMessage();
		StringBuilder msg = new StringBuilder(em.getContentRaw().toString());
		long userID = event.getAuthor().getIdLong();
		System.out.println(msg);
		if(newDiscordUserID == userID) {
			newDiscordUserID = 0;
			/* if (PlayerBuilder.getPlayer(msg) == null) {
				em.reply("Player name **" + msg.toString() + "** doesn't exist. Please play a tournament first or contact your TO's.").queue();
				return;
			} */
			PlayerBuilder.addDiscordPlayer(String.valueOf(userID), msg.toString());
			em.reply("Your Discord account is now registered as " + msg.toString()).queue();
		}
	}

    @Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		
		String command = event.getName();
		long userID = event.getUser().getIdLong();

        System.out.println(command);
        if(command.equalsIgnoreCase("gettourney")) {
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

		if(command.equalsIgnoreCase("addtourneychallonge")) {
			event.deferReply().queue();
			String name = event.getOption("name").getAsString();
			String slugLink = event.getOption("link").getAsString();
            boolean online = event.getOption("online").getAsBoolean();
			int idStart = slugLink.indexOf("com/") + 4;
			String id = slugLink.substring(idStart).split("/")[0];
			slugLink = slugLink.substring(0, idStart + id.length());
			try {
				TourneyBuilder.addTourneyChallonge(id, name, online, slugLink);
				event.getHook().sendMessage("Tournament added!").queue();
			} catch (Exception e) {
				String message = e.getMessage();
				if (message.equals(("Scores"))) {
					event.getHook().sendMessage("Bad scores.").queue();
				} else if (message.equals("duplicate")) {
					event.getHook().sendMessage("Error: duplicate names").queue();
				} else {
					event.getHook().sendMessage("finished with Unknown error. Check logs").queue();
					e.printStackTrace();
				}
			}
		}

		if(command.equalsIgnoreCase("addtourneystartgg")) {
			event.deferReply().queue();
			String name = event.getOption("name").getAsString();
			String slugLink = event.getOption("link").getAsString();
            boolean online = event.getOption("online").getAsBoolean();
			int idStart = slugLink.indexOf(".gg/") + 4;
			slugLink = slugLink.substring(idStart);
			String[] id = slugLink.split("/");
			slugLink = "tournament" + "/" + id[1] + "/" + "event" + "/" + id[3];
			System.out.println(slugLink);
			try {
				TourneyBuilder.addTourneyStart(slugLink, name, online);
				event.getHook().sendMessage("Tournament added!").queue();
			} catch (Exception e) {
				String message = e.getMessage();
				if (message.equals(("Scores"))) {
					event.getHook().sendMessage("Bad scores.").queue();
				} else if (message.equals("duplicate")) {
					event.getHook().sendMessage("Error: duplicate names").queue();
				} else {
					event.getHook().sendMessage("finished with Unknown error. Check logs").queue();
					e.printStackTrace();
				}
			}
		}

		if(command.equalsIgnoreCase("playercard")) {
			OptionMapping player = event.getOption("player");
			try {
				if(player == null) {
					String p = PlayerBuilder.getDiscordPlayer(String.valueOf(userID));
					
					if(p == null) {
						System.out.println(p);
						newDiscordUserID = userID;
						event.reply("I have not seen you before. Please give me your gamer tag.").queue();
						ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
						scheduler.schedule(new Runnable() {public void run() {
							newDiscordUserID = 0;
						}}, 15, TimeUnit.MINUTES);
						return;
					}
					
					File f = PlayerBuilder.getPlayerCard(p);
					event.replyFiles(FileUpload.fromData(f)).queue();
					return;
				}
				StringBuilder playerName = new StringBuilder(player.getAsString());
				if (PlayerBuilder.getPlayer(playerName) == null) {
					event.reply("Player doesn't exist: " + playerName.toString()).queue(); 
					return;
				}
				File ans = PlayerBuilder.getPlayerCard(playerName.toString());
				event.replyFiles(FileUpload.fromData(ans)).queue();
				return;
			} catch(Exception e) {
				e.printStackTrace();
				String msg = e.getMessage();
				
				if (msg == null) {
					//e.printStackTrace();
					event.reply("unknown error occured.").queue();
				} else {
					event.reply("Player doesn't exist: " + msg).queue(); 
				}
			}

		}

		if(command.equalsIgnoreCase("updatecard")) {
			try {
				String p = PlayerBuilder.getDiscordPlayer(String.valueOf(userID));
					
				if(p == null) {
					System.out.println(p);
					newDiscordUserID = userID;
					event.reply("I have not seen you before. Please give me your gamer tag.").queue();
					ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
					scheduler.schedule(new Runnable() {public void run() {
						newDiscordUserID = 0;
					}}, 15, TimeUnit.MINUTES);
					return;
				}
				OptionMapping town = event.getOption("town");
				OptionMapping mains = event.getOption("mains");
				OptionMapping bio = event.getOption("bio");
				if (town == null && mains == null && bio == null) {
					event.reply("No changes made.").queue();
					return;
				}
				String ans = "";
				if (town != null) {
					PlayerBuilder.changePlayer(p, "town", town.getAsString());
					ans += "town ";
				}
				if (mains != null) {
					PlayerBuilder.changePlayer(p, "mains", mains.getAsString());
					ans += "mains ";
				}
				if (bio != null) {
					PlayerBuilder.changePlayer(p, "bio", bio.getAsString());
					ans += "bio ";
				}
				event.reply("Updated: " + ans).queue();
			} catch(Exception e) {
				e.printStackTrace();
				String msg = e.getMessage();
				if (msg == null) {
					event.reply("unknown error occured.").queue();
				}
			}
		}

		if(command.equalsIgnoreCase("addplayer")) {
			String name = event.getOption("name").getAsString();
			String town = event.getOption("town").getAsString();
			OptionMapping mains = event.getOption("mains");
			OptionMapping aliases = event.getOption("aliases");
			String[] tempMains;
			String[] tempAliases;
			
			if (mains == null) {
				tempMains = new String[] {};
			} else {
				tempMains = mains.getAsString().split(" ");
			}
			if (aliases == null) {
				tempAliases = new String[] {};
			} else {
				tempAliases = aliases.getAsString().split(" ");
			}
			try {
				PlayerBuilder.addPlayer(name.toLowerCase(), town.toLowerCase(), tempAliases, tempMains);
				event.reply("Player Added.").queue();
			} catch (Exception e) {
				e.printStackTrace();
				String msg = e.getMessage();
				
				if (msg == null) {
					//e.printStackTrace();
					event.reply("unknown error occured.").queue();
				} else {
					event.reply("Player doesn't exist: " + msg).queue(); 
				}
			}
		}

		if(command.equalsIgnoreCase("addaliases")) {
			String[] aliases = event.getOption("aliases").getAsString().split(" ");
			StringBuilder name = new StringBuilder(event.getOption("name").getAsString());
			
			try {
				PlayerBuilder.addAlias(name, aliases);
				event.reply("Aliases added for " + name.toString()).queue();
			} catch (Exception e) {
				e.printStackTrace();
				String msg = e.getMessage();
				
				if (msg == null) {
					//e.printStackTrace();
					event.reply("unknown error occured.").queue();
				} else if (msg.equals("name")) {
					event.reply("Player doesn't exist: " + name.toString()).queue(); 
				} else {
					event.reply("Error: name or alias already exists: " + msg).queue();
				}
			}
		}

        /* if (command.equalsIgnoreCase("showrankings")) {
			event.reply(JsonBuilder.getRankings()).queue();
		} */

		if (command.equalsIgnoreCase("showtiers")) {
			OptionMapping o = event.getOption("town");
			String answer = "**Rankings for ";
			ArrayList<Set<String>> tiers;
			if (o != null) {
				String town = o.getAsString();
				answer += town + "\n\n";
				tiers = PlayerBuilder.getAllTiers(town);
			} else {
				answer += "NC" + "\n\n";
				tiers = PlayerBuilder.getAllTiers();
			}
			for (int i = 0; i < PlayerBuilder.tiers.length(); ++i) {
				answer += PlayerBuilder.tiers.charAt(i) + "\n```";
				for(String name : tiers.get(i)) {
					answer += name + "\n";
				}
				answer += "```\n";
			}
			event.reply(answer);
		}

        if (command.equalsIgnoreCase("record")) {
			try {
                String player1 = event.getOption("player1").getAsString();
                String player2 = event.getOption("player2").getAsString();
				int[] record = PlayerBuilder.getRecord(player1, player2);
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

        /*if (command.equalsIgnoreCase("aliases")) {
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
		}*/

        if (command.equalsIgnoreCase("hotness")) {
			try {
                StringBuilder player1 = new StringBuilder(event.getOption("player1").getAsString());
                StringBuilder player2 = new StringBuilder(event.getOption("player2").getAsString());
				double hotness = PlayerBuilder.hotness(player1, player2);
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

        if (userID != magID) return;

        System.out.println(event.getUser().getAsTag());
        if(command.equalsIgnoreCase("restart")) {
			try {
				BotStartup.restart();
			} catch (Exception e) {
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

		if(command.equalsIgnoreCase("addseason")) {
			String name = event.getOption("name").getAsString();
			int day = event.getOption("day").getAsInt();
			int month = event.getOption("month").getAsInt();
			int year = event.getOption("year").getAsInt();
			int dayEnd = event.getOption("day2").getAsInt();
			int monthEnd = event.getOption("month2").getAsInt();
			int yearEnd = event.getOption("year2").getAsInt();
			SeasonBuilder.addSeason(name, day, month, year, dayEnd, monthEnd, yearEnd);
			event.reply("Season added!").queue();
		}

		if(command.equalsIgnoreCase("changeseason")) {
			Set<String> seasons = SeasonBuilder.getSeasons();
			Builder select = StringSelectMenu.create("change season");
			for(String name : seasons) {
				select.addOption(name, name);
			}

			event.reply("Choose a season")
                .addActionRow(select.build())
                .queue();
		}

		if(command.equalsIgnoreCase("playergraph")) {
			OptionMapping player = event.getOption("player");
			File f = new File("maggraph.png");
			event.replyFiles(FileUpload.fromData(f)).queue();
			
		}
	}

	@Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("change season")) {
            String name = event.getValues().get(0);
			int[] season = SeasonBuilder.getSeason(name);
			TextInput newName = TextInput.create("name", "New name", TextInputStyle.SHORT)
                    .setPlaceholder(name)
                    .build();
			TextInput day = TextInput.create("day", "start day", TextInputStyle.SHORT)
                    .setPlaceholder(String.valueOf(season[0]))
                    .build();
			TextInput month = TextInput.create("month", "start month", TextInputStyle.SHORT)
                    .setPlaceholder(String.valueOf(season[1]))
                    .build();
			TextInput year = TextInput.create("year", "start year", TextInputStyle.SHORT)
                    .setPlaceholder(String.valueOf(season[2]))
                    .build();
			TextInput dayEnd = TextInput.create("dayEnd", "end day", TextInputStyle.SHORT)
                    .setPlaceholder(String.valueOf(season[3]))
                    .build();
			TextInput monthEnd = TextInput.create("monthEnd", "end month", TextInputStyle.SHORT)
                    .setPlaceholder(String.valueOf(season[4]))
                    .build();
			TextInput yearEnd = TextInput.create("yearEnd", "end year", TextInputStyle.SHORT)
                    .setPlaceholder(String.valueOf(season[5]))
                    .build();
            Modal modal = Modal.create("changeSeason " + name, "Update a Season")
                    .addActionRows(ActionRow.of(day), ActionRow.of(month), ActionRow.of(year), ActionRow.of(dayEnd), ActionRow.of(monthEnd), ActionRow.of(yearEnd), ActionRow.of(newName))
                    .build();
			event.replyModal(modal);
        }
    }

	@Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		String[] modalArgs = event.getModalId().split(" ");
        if (modalArgs[0].equals("changeSeason")) {
            int day = Integer.parseInt(event.getValue("day").getAsString());
			int month = Integer.parseInt(event.getValue("month").getAsString());
			int year = Integer.parseInt(event.getValue("year").getAsString());
			int dayEnd = Integer.parseInt(event.getValue("dayEnd").getAsString());
			int monthEnd = Integer.parseInt(event.getValue("monthEnd").getAsString());
			int yearEnd = Integer.parseInt(event.getValue("yearEnd").getAsString());
			String newName = event.getValue("name").getAsString();
			String name = modalArgs[1];


			SeasonBuilder.changeSeason(name, newName, day, month, year, dayEnd, monthEnd, yearEnd);
            event.reply("Thanks for your request!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

		OptionData aliases = new OptionData(OptionType.STRING, "aliases", "no punctuation, \"hbox hungrybox\"", false);
		OptionData aliasesRequired = new OptionData(OptionType.STRING, "aliases", "no punctuation, \"hbox hungrybox\"", true);
		OptionData mains = new OptionData(OptionType.STRING, "mains", "no punctuation, \"falco fox marth\"", false);
		OptionData town = new OptionData(OptionType.STRING, "town", "local, region, or city", false);
		OptionData townRequired = new OptionData(OptionType.STRING, "town", "local, region, or city", true);
		OptionData name = new OptionData(OptionType.STRING, "name", "Name", true);
		OptionData month = new OptionData(OptionType.INTEGER, "month", "Month", true);
		OptionData day = new OptionData(OptionType.INTEGER, "day", "Day", true);
		OptionData year = new OptionData(OptionType.INTEGER, "year", "Year", true);
		OptionData month2 = new OptionData(OptionType.INTEGER, "month2", "End Month", true);
		OptionData day2 = new OptionData(OptionType.INTEGER, "day2", "End Day", true);
		OptionData year2 = new OptionData(OptionType.INTEGER, "year2", "End Year", true);
        OptionData player1 = new OptionData(OptionType.STRING, "player1", "Player 1", true);
        OptionData player2 = new OptionData(OptionType.STRING, "player2", "Player 2", true);
        OptionData player = new OptionData(OptionType.STRING, "player", "Player name. Defaults to you if blank.", false);
        OptionData slugLink = new OptionData(OptionType.STRING, "link", "https://www.start.gg/tournament/melee-mondays-weekly-1-picantetcg/event/melee-singles/", true);
		OptionData bio = new OptionData(OptionType.STRING, "bio", "your player card bio", false);
        OptionData online = new OptionData(OptionType.BOOLEAN, "online", "Was the tournament Online?", true);
		commandData.add(Commands.slash("changeseason", "Change a season after selecting its name."));
		commandData.add(Commands.slash("addseason", "Enter start and end date of a new season.").addOptions(name, day, month, year, day2, month2, year2));
		commandData.add(Commands.slash("gettourney", "Get a CSV file of sets from a tourney.").addOptions(slugLink, online));
		commandData.add(Commands.slash("addtourneychallonge", "Add a challonge tournament to the database.").addOptions(slugLink, name, online));
		commandData.add(Commands.slash("addtourneystartgg", "Add a Start.gg tournament to the database.").addOptions(slugLink, name, online));
		commandData.add(Commands.slash("addplayer", "Add a player to the database.").addOptions(townRequired, name, mains, aliases));
		commandData.add(Commands.slash("addaliases", "one or more aliases.").addOptions(name, aliasesRequired));
		commandData.add(Commands.slash("playercard", "Virtual player card.").addOptions(player));
		commandData.add(Commands.slash("updatecard", "Update your player info.").addOptions(mains, town, bio));

        commandData.add(Commands.slash("record", "Get the head-to-head record of two players.").addOptions(player1, player2));
        commandData.add(Commands.slash("hotness", "How 'HOT' would this set be? Give me two players.").addOptions(player1, player2));
        commandData.add(Commands.slash("aliases", "Get other nicknames of a player.").addOptions(player));
        commandData.add(Commands.slash("showrankings", "Get my Elo rankings for NC players. Provide a town for local rankings").addOptions(town));
		commandData.add(Commands.slash("showtiers", "Get the head-to-head tierlist for NC players. Provide a town for local tiers").addOptions(town));
        commandData.add(Commands.slash("restart", "Restart the bot."));
        commandData.add(Commands.slash("remake", "Remake the derived json data."));
        commandData.add(Commands.slash("shutdown", "Shutdown the bot."));
		commandData.add(Commands.slash("playergraph", "Get an Elo graph for a player").addOptions(player));
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