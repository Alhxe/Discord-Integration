package di.dilogin.discord.command;

import java.awt.Color;
import java.util.List;

import di.dilogin.controller.MainController;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import di.internal.entity.DiscordCommand;
import di.internal.entity.DiscordSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * A command to show registered users paginated.
 */
public class UserListDiscordCommand implements DiscordCommand, DiscordSlashCommand {

    private static final int USERS_PER_PAGE = 10;
    private static final Color EMBED_COLOR = new Color(0x4287F5);

    private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

    @Override
    public void execute(String message, MessageReceivedEvent event) {
        int pageNumber = 1;
        try {
            pageNumber = Integer.parseInt(message);
        } catch (NumberFormatException ignored) {
            // Invalid page number format, default to page 1
        }
        sendPage(event, pageNumber);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int pageNumber = 1;
        event.deferReply().setEphemeral(true).queue();
        try {
        	String pageOption = event.getOption("page").getAsString();
            pageNumber = Integer.parseInt(pageOption);
        } catch (Exception e) {
            // Invalid page number format, default to page 1
        }
        sendPage(event, pageNumber);
    }

    private void sendPage(Object event, int pageNumber) {
        List<DIUser> users = userDao.getAllUsers();
        int totalPages = (int) Math.ceil((double) users.size() / USERS_PER_PAGE);
        if (pageNumber < 1 || pageNumber > totalPages) {
            pageNumber = 1; // Reset to first page if out of range
        }

        List<DIUser> usersOnPage = users.subList((pageNumber - 1) * USERS_PER_PAGE,
                Math.min(users.size(), pageNumber * USERS_PER_PAGE));

        StringBuilder userListString = new StringBuilder();
        for (DIUser user : usersOnPage) {
            User discordUser = user.getPlayerDiscord().orElse(null);
            if (discordUser != null) {
                userListString.append(discordUser.getName()).append(" - ").append(discordUser.getAsMention()).append("\n");
            }
        }

        String title = LangController.getString("userlist_discord_title");
        String description = LangController.getString("userlist_discord_description")
                .replace("%page%", String.valueOf(pageNumber))
                .replace("%total%", String.valueOf(totalPages))
                .replace("%totalusers%", String.valueOf(users.size()));
        String footer = LangController.getString("userlist_discord_footer")
                .replace("%page%", String.valueOf(pageNumber))
                .replace("%total%", String.valueOf(totalPages))
                .replace("%totalusers%", String.valueOf(users.size()));

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(EMBED_COLOR)
                .setTitle(title)
                .setDescription(description)
                .addField("Users", userListString.toString(), false)
                .setFooter(footer);

        MessageEmbed messageEmbed = embedBuilder.build();

        if (event instanceof MessageReceivedEvent) {
            ((MessageReceivedEvent) event).getChannel().sendMessageEmbeds(messageEmbed).queue();
        } else if (event instanceof SlashCommandInteractionEvent) {
            ((SlashCommandInteractionEvent) event).getHook().sendMessageEmbeds(messageEmbed).setEphemeral(true).queue();
        }
    }

    @Override
    public String getAlias() {
        return CommandAliasController.getAlias("userlist_discord_command");
    }
}
