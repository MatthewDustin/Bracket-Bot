import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
import javax.security.auth.login.LoginException;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	public final String prefix = "!";
	public final long magID = 183728250822983680L;
	public final long netplayChannel = 438192901772541972L;
	public final long booneServer = 413447552843776003L;
	private final String help = "```Welcome to BracketBot by Mag v1.0\n\nCommands:\n!h, !help\t\tShows this message\n!showrankings\tdisplays live ELO rankings\n!game\t\t\tUsage: !game name1 name2 score1 score2\n\t\t\t\t People have multiple names and this bot recognizes most of them\n\t\t\t\t order doesn't matter but scores must match names\n!aliases\t\t Usage: !aliases name\n\t\t\t\t shows all nicknames of a player```**TIPS**\n-Please be careful entering games, a misreported game could go unnoticed by me and that would be pretty bad\n-if you have problems/questions please DM Mag#8928\n-Please DM the bot most of your commands **instead of spamming the server** (use commands in #netplay otherwise)\n-commands in DMs don't need a prefix";
	private final String helpGame = "Usage: !game name1 name2 score1 score2";
	private final String helpAliases = "Usage: '!aliases name'";
	private final String helpShowRankings = "shows elo rankings";
	private List<String> doneGames = new ArrayList<String>();
	private Timer timer;
	
	
	

	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getChannelType().isGuild()) return;
		String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ");
		System.out.println(Arrays.toString(args));
		if (args[0].equalsIgnoreCase(prefix + "test")) {
			event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("hello")).queue();
		}
		
		if (args[0].equals(prefix + "help") || args[0].equals(prefix + "h") || args[0].equals("help") || args[0].equals("h")) {
			if (args.length == 1) {
				event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(help)).queue();
			} else {
				switch (args[1]) {
					case prefix + "game":
					case "game":
					case prefix + "g":
					case "g":
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(helpGame)).queue();
						break;
					case prefix +"aliases":
					case "aliases":
					case prefix + "a":
					case "a":
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(helpAliases)).queue();
						break;
					case prefix + "showrankings":
					case "showrankings":
					case prefix + "sr":
					case "sr":
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(helpShowRankings)).queue();
						break;
					default:
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("No such command: !" + args[1])).queue();
				}
			}
		}
		
		if (args[0].equals(prefix + "record") || args[0].equals("record")) {
			try {
				if(args.length == 3) {
					int[] record = JsonBuilder.getRecord(args[1], args[2]);
					EmbedBuilder eb = new EmbedBuilder();
					eb.setColor(Color.red);
					eb.setTitle(args[1] + " vs. " + args[2], null);
					eb.addField("Games", String.valueOf(record[0]), true);
					eb.addField(args[1] + " dubs", String.valueOf(record[1]), true);
					eb.addField(args[2] + " dubs", String.valueOf(record[2]), true);
					
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage((CharSequence) eb.build())).queue(); 
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String msg = e.getMessage();
				if (msg == null) {
					e.printStackTrace();
				} else if (msg.equals("none")) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("No matches exist yet.")).queue(); 
				}
			}
		}
		
		if (args[0].equals(prefix + "hotness") || args[0].equals("hotness") || args[0].equals("hot") || args[0].equals(prefix + "hot")) {
			try {
				if(args.length == 3) {
					double hotness = JsonBuilder.hotness(args[1], args[2]);
					System.out.println(hotness);
					if (hotness > 4 && hotness < 6) {
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(":fire: **" + args[1] + "** vs. **" + args[2] + "** is a very Hot Set!:fire:")).queue(); 
					} else if (hotness > 3 && hotness < 7) {
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("**" + args[1] + "** vs. **" + args[2] + "** is a Hot Set!")).queue(); 
					}
					else {
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Hotness inconclusive")).queue(); 
					}
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		
		if (args[0].equals(prefix + "addtourney") || args[0].equals("addtourney")) {
			try {
				if (args.length == 3 && (args[2].equals("true") || args[2].equals("false"))) {
					JsonBuilder.addTourney(args[1], Boolean.valueOf(args[2]));
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Games added!")).queue();
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if(message.equals("args")) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Incorrect command. Usage: !addtourney [id] [true or false]")).queue();
				} else if (message.equals(("Scores"))) {
					event.getChannel().sendMessage("Bad scores. Usage: '.game name1 name2 score score'").queue();
				} else if (message.equals("duplicate")) {
					event.getChannel().sendMessage("Error: duplicate names").queue();
				} else {
					event.getChannel().sendMessage("finished with Unknown error. Check logs").queue();
					e.printStackTrace();
				}
			}
		}
		
		if (args[0].equals(prefix + "get") || args[0].equals("get")) {
			//EventWaiter waiter = new EventWaiter();
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
								event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(":fire: **" + s[0] + "** vs. **" + s[1] + "** is a very Hot Set!:fire:")).queue(); 
							} else if (hotness > 3 && hotness < 7) {
								event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("**" + s[0] + "** vs. **" + s[1] + "** is a Hot Set!")).queue(); 
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 30000);
		}
		
		if (args[0].equals(prefix + "showrankings") || args[0].equals("showrankings") || args[0].equals("sr") || args[0].equals(prefix + "sr")) {
			event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(JsonBuilder.getRankings())).queue();
		}
		
		if (args[0].equals(prefix + "aliases") || args[0].equals("aliases") || args[0].equals("a") || args[0].equals(prefix + "a")) {
			try {
				if(args.length == 2) {
					String ans = JsonBuilder.getAliases(args[1]);
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Aliases: " + ans)).queue();
				}
				else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if(message.equals("args")) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Incorrect command. Usage: '!aliases name'")).queue();
				} else if (message.equals("none")) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(args[1] + " does not exist")).queue();
				}
				else {
					e.printStackTrace();
				}
			}
		}
		
		if (args[0].equals(prefix + "game") || args[0].equals("game") || args[0].equals("g") || args[0].equals(prefix + "g")) {
			try {
				if(args.length == 6 && (args[5].equals("true") || args[5].equals("false"))) {
					JsonBuilder.simulateSet(new StringBuilder(args[1]), new StringBuilder(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), false, Boolean.valueOf(args[5]));
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Game recorded!")).queue();
				}
				else {
					throw new Exception("args");
				}
			} catch(Exception e) {
				String message = e.getMessage();
				if (message == null || message.equals("args")) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Incorrect command. Usage: '!game name1 name2 score score'")).queue();
					e.printStackTrace();
				} else if (message.equals(args[1]) || message.equals(args[2])) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Bad name: " + message)).queue();
				} else if (message.equals("Scores") || e instanceof NumberFormatException) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Bad scores. Usage: '!game name1 name2 score score'")).queue();
				} else if (message.equals("duplicate")) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Error: duplicate names")).queue();
				} else {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Incorrect command. Usage: '!game name1 name2 score score'")).queue();
					e.printStackTrace();
				}
			}
		}
		
		if (event.getAuthor().getIdLong() != magID) return;
		
		if(args[0].equals(prefix + "clearmatches") || args[0].equals("clearmatches")) {
			doneGames = new ArrayList<String>();
			timer.cancel();
			event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("API Challonge matches cleared")).queue();
		}
		
		if(args[0].equals(prefix + "restart") || args[0].equals("restart")) {
			try {
				BotStartup.restart();
			} catch (LoginException e) {
				e.printStackTrace();
			}
		}
		
		if(args[0].equals(prefix + "addplayer") || args[0].equals("addplayer") || args[0].equals("ap") || args[0].equals(prefix + "ap")) {
			String[] aliases = Arrays.copyOfRange(args, 3, args.length);
			try {
				if(args.length >= 3 && (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))) {
					JsonBuilder.addPlayer(args[1], Boolean.valueOf(args[2]), aliases, false);
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Player " + args[1] + " added!")).queue();
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if (message == null) {
					e.printStackTrace();
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Couldn't write to file")).queue();
				} else if(message.equals("args")) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Incorrect command. Usage: '!addplayer name [true or false] (alias1 alias2...)'")).queue();
				} else {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Error. Alias: " + message + " already exists.")).queue();
				}
			}
		}
		if (args[0].equals(prefix + "remake") || args[0].equals("remake")) {
			try {
				JsonBuilder.remakeFile();
				event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Database remade!")).queue();
			} catch(Exception e) {
				event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Finished with errors (check console)")).queue();
				e.printStackTrace();
			}
		}
		if(args[0].equals(prefix + "shutdown") || args[0].equals("shutdown")) {
			event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Goodbye!")).queue();
			BotStartup.shutdown();
		}
		
		if(args[0].equals(prefix + "playercard")) {
			try {
				if (args.length > 1) {
					
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				
			}
		}
		
		if(args[0].equals(prefix + "newplayer")) {
			
		}
	}

	/* public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		//if (event.getAuthor().getIdLong() != magID) return;
		
		String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ");
		
		if (args[0].equals(prefix + "help") || args[0].equals(prefix + "h")) {
			event.getMessage().delete().queue();
			if (args.length == 1) {
				event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(help)).queue();
			} else {
				switch (args[1]) {
					case prefix + "game":
					case "game":
					case prefix + "g":
					case "g":
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(helpGame)).queue();
						break;
					case prefix +"aliases":
					case "aliases":
					case prefix + "a":
					case "a":
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(helpAliases)).queue();
						break;
					case prefix + "showrankings":
					case "showrankings":
					case prefix + "sr":
					case "sr":
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(helpShowRankings)).queue();
						break;
					default:
						event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(help)).queue();
				}
			}
		}
		
		//Doesn't allow bot in other channels in Boone Server 
		
		if (event.getChannel().getIdLong() != netplayChannel && event.getGuild().getIdLong() == booneServer) return;
		
		
		if (args[0].equals(prefix + "showrankings") || args[0].equals(prefix + "sr")) {
			event.getChannel().sendMessage(JsonBuilder.getRankings()).queue();
		}
		
		if (args[0].equals(prefix + "game") || args[0].equals(prefix + "g")) {
			try {
				if(args.length == 6 && (args[5].equals("true") || args[5].equals("false"))) {
					JsonBuilder.simulateSet(new StringBuilder(args[1]), new StringBuilder(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), false, Boolean.valueOf(args[5]));
					event.getMessage().reply("Game recorded!").queue();
				}
				else {
					throw new Exception("args");
				}
			} catch(Exception e) {
				String message = e.getMessage();
				if (message == null || message.equals("args")) {
					event.getChannel().sendMessage("Incorrect command. Usage: '!game name1 name2 score score'").queue();
					e.printStackTrace();
				} else if (message.equals(args[1]) || message.equals(args[2])) {
					event.getChannel().sendMessage("Bad name: " + message).queue();
				} else if (message.equals("Scores") || e instanceof NumberFormatException) {
					event.getChannel().sendMessage("Bad scores. Usage: '!game name1 name2 score score'").queue();
				} else if (message.equals("duplicate")) {
					event.getChannel().sendMessage("Error: duplicate names").queue();
				} else {
					event.getChannel().sendMessage("Incorrect command. Usage: '!game name1 name2 score score'").queue();
					e.printStackTrace();
				}
			}
		}
		
		if (args[0].equals(prefix + "aliases")) {
			System.out.println(Arrays.toString(args));
			try {
				if(args.length == 2) {
					String ans = JsonBuilder.getAliases(args[1]);
					event.getChannel().sendMessage("Aliases: " + ans).queue();
				}
				else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if(message.equals("args")) {
					event.getChannel().sendMessage("Incorrect command. Usage: '!aliases name'").queue();
				} else if (message.equals("none")) {
					event.getChannel().sendMessage(args[1] + " does not exist").queue();
				}
				e.printStackTrace();
			}
		}
		
		if (args[0].equals(prefix + "record")) {
			try {
				if(args.length == 3) {
					int[] record = JsonBuilder.getRecord(args[1], args[2]);
					EmbedBuilder eb = new EmbedBuilder();
					eb.setColor(Color.red);
					eb.setTitle(args[1] + " vs. " + args[2], null);
					eb.addField("Games", String.valueOf(record[0]), true);
					eb.addField(args[1] + " dubs", String.valueOf(record[1]), true);
					eb.addField(args[2] + " dubs", String.valueOf(record[2]), true);
					
					event.getChannel().sendMessage(eb.build()).queue(); 
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String msg = e.getMessage();
				if (msg == null) {
					e.printStackTrace();
				} else if (msg.equals("none")) {
					event.getChannel().sendMessage("No matches exist yet.").queue(); 
				}
			}
		}
		
		if(args[0].equals(prefix + "addplayer")) {
			String[] aliases = Arrays.copyOfRange(args, 3, args.length);
			try {
				JsonBuilder.addPlayer(args[1], Boolean.valueOf(args[2]), aliases, false);
				event.getChannel().sendMessage("Player " + args[1] + " added!").queue();
			} catch (Exception e) {
				String message = e.getMessage();
				if (message.equals("exists")) {
					
				} else {
					e.printStackTrace();
					event.getChannel().sendMessage("Couldn't write to file").queue();
				}
			}
		}
		
		if (args[0].equals(prefix + "remake")) {
			try {
				JsonBuilder.remakeFile();
				event.getChannel().sendMessage("Database remade!").queue();
			} catch(Exception e) {
				event.getChannel().sendMessage("Finished with errors (check console)").queue();
				e.printStackTrace();
			}
		}
		
		if(args[0].equals(prefix + "shutdown")) {
			event.getChannel().sendMessage("Goodbye!").queue();
			BotStartup.shutdown();
		}
		if (args[0].equals(prefix + "get")) {
			//EventWaiter waiter = new EventWaiter();
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
								event.getChannel().sendMessage(":fire: **" + s[0] + "** vs. **" + s[1] + "** is a very Hot Set!:fire:").queue(); 
							} else if (hotness > 3 && hotness < 7) {
								event.getChannel().sendMessage("**" + s[0] + "** vs. **" + s[1] + "** is a Hot Set!").queue(); 
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 30000);
		}
		if (args[0].equals(prefix + "hotness") || args[0].equals(prefix + "hot")) {
			try {
				if(args.length == 3) {
					double hotness = JsonBuilder.hotness(args[1], args[2]);
					System.out.println(hotness);
					if (hotness > 4 && hotness < 6) {
						event.getChannel().sendMessage(":fire: **" + args[1] + "** vs. **" + args[2] + "** is a very Hot Set!:fire:").queue(); 
					} else if (hotness > 3 && hotness < 7) {
						event.getChannel().sendMessage("**" + args[1] + "** vs. **" + args[2] + "** is a Hot Set!").queue(); 
					}
					else {
						event.getChannel().sendMessage("Hotness inconclusive").queue(); 
					}
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		if (args[0].equals(prefix + "addtourney") || args[0].equals("addtourney")) {
			try {
				if (args.length == 3 && (args[2].equals("true") || args[2].equals("false"))) {
					JsonBuilder.addTourney(args[1], Boolean.valueOf(args[2]));
					event.getChannel().sendMessage("Games added!").queue();
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if(message.equals("args")) {
					event.getChannel().sendMessage("Incorrect command. Usage: !addtourney [id] [true or false]").queue();
				} else if (message.equals(("Scores"))) {
					event.getChannel().sendMessage("Bad scores. Usage: '.game name1 name2 score score'").queue();
				} else if (message.equals("duplicate")) {
					event.getChannel().sendMessage("Error: duplicate names").queue();
				} else {
					event.getChannel().sendMessage("finished with Unknown error. Check logs").queue();
					e.printStackTrace();
				}
			}
		}
		
		if (args[0].equals(prefix + "gettourney") || args[0].equals("gettourney")) {
			try {
				if (args.length == 3 && (args[2].equals("true") || args[2].equals("false"))) {
					String msg = JsonBuilder.getTourney(args[1], Boolean.valueOf(args[2]));
					event.getChannel().sendMessage(msg).queue();
				} else {
					throw new Exception("args");
				}
			} catch (Exception e) {
				String message = e.getMessage();
				if(message.equals("args")) {
					event.getChannel().sendMessage("Incorrect command. Usage: !gettourney [id] [true or false]").queue();
				} else if (message.equals(("Scores"))) {
					event.getChannel().sendMessage("Bad scores. Usage: '.game name1 name2 score score'").queue();
				} else if (message.equals("duplicate")) {
					event.getChannel().sendMessage("Error: duplicate names").queue();
				} else {
					event.getChannel().sendMessage("finished with Unknown error. Check logs").queue();
					e.printStackTrace();
				}
			}
		}
		if(args[0].equals(prefix + "restart")) {
			try {
				BotStartup.restart();
			} catch (LoginException e) {
				e.printStackTrace();
			}
		}
	} */
}
