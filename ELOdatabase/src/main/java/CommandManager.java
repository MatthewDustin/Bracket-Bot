import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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

    @Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		String command = event.getName();
		long userID = event.getUser().getIdLong();

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
			String player = event.getOption("player").getAsString();
			File f = ChartUtils.buff
			event.reply
			
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

		OptionData name = new OptionData(OptionType.STRING, "name", "Name", true);
		OptionData month = new OptionData(OptionType.INTEGER, "month", "Month", true);
		OptionData day = new OptionData(OptionType.INTEGER, "day", "Day", true);
		OptionData year = new OptionData(OptionType.INTEGER, "year", "Year", true);
		OptionData month2 = new OptionData(OptionType.INTEGER, "month2", "End Month", true);
		OptionData day2 = new OptionData(OptionType.INTEGER, "day2", "End Day", true);
		OptionData year2 = new OptionData(OptionType.INTEGER, "year2", "End Year", true);
        OptionData player1 = new OptionData(OptionType.STRING, "player1", "Player 1", true);
        OptionData player2 = new OptionData(OptionType.STRING, "player2", "Player 2", true);
        OptionData player = new OptionData(OptionType.STRING, "player", "Player Name. Defaults to you if blank.", false);
        OptionData slugLink = new OptionData(OptionType.STRING, "link", "https://www.start.gg/tournament/melee-mondays-weekly-1-picantetcg/event/melee-singles/", true);
        OptionData online = new OptionData(OptionType.BOOLEAN, "online", "Was the tournament Online?", true);
		commandData.add(Commands.slash("changeseason", "Change a season after selecting its name."));
		commandData.add(Commands.slash("addseason", "Enter start and end date of a new season.").addOptions(name, day, month, year, day2, month2, year2));
		commandData.add(Commands.slash("addtourney", "Get a CSV file of sets from a tourney.").addOptions(slugLink, online));
        commandData.add(Commands.slash("record", "Get the head-to-head record of two players.").addOptions(player1, player2));
        commandData.add(Commands.slash("hotness", "How 'HOT' would this set be? Give me two players.").addOptions(player1, player2));
        commandData.add(Commands.slash("aliases", "Get other nicknames of a player.").addOptions(player));
        commandData.add(Commands.slash("showrankings", "Get my Elo rankings for NC players."));
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