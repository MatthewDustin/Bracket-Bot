import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

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
                
                String title = Startgg.getStartgg(slugLink, online.getAsBoolean());
                File file = new File(title + ".csv");
                FileUpload upload = FileUpload.fromData(file);
                event.reply(title + "...finished").addFiles(upload).queue();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        OptionData slugLink = new OptionData(OptionType.STRING, "link", "https://www.start.gg/tournament/melee-mondays-weekly-1-picantetcg/events/melee-singles/", true);
        OptionData online = new OptionData(OptionType.BOOLEAN, "online", "Was the tournament Online?", true);
        commandData.add(Commands.slash("addtourney", ".").addOptions(slugLink, online));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
