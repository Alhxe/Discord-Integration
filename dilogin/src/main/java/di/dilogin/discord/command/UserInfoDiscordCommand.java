package di.dilogin.discord.command;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import di.dilogin.controller.MainController;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.discord.util.DiscordMessageDeleter;
import di.dilogin.entity.DIUser;
import di.internal.entity.DiscordCommand;
import di.internal.entity.DiscordSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.data.DataObject;

/**
 * A command to retrieve user information from the Discord server.
 */
public class UserInfoDiscordCommand implements DiscordCommand, DiscordSlashCommand {

    /**
     * Data Access Object for managing user information.
     */
    private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

    /**
     * Executes the user info command.
     * 
     * @param message The message containing the command and its arguments.
     * @param event The event triggered when the command is received.
     */
    @Override
    public void execute(String message, MessageReceivedEvent event) {
        DiscordMessageDeleter.deleteMessage(20, event instanceof MessageReceivedEvent ? ((MessageReceivedEvent) event).getMessage() : null);
        List<User> userList = event.getMessage().getMentions().getUsers();
        try {
            MessageEmbed embed = EmbedBuilder.fromData(DataObject.fromJson(handleUserInformation(event.getAuthor(), event.getChannel(), message, userList))).build();
            sendEmbedMessage(event.getChannel(), embed);
        } catch (Exception e) {
            sendMessage(event.getChannel(), handleUserInformation(event.getAuthor(), event.getChannel(), message, userList));
        }
    }

    /**
     * Executes the user info command for SlashCommandInteractionEvent.
     * 
     * @param event The SlashCommandInteractionEvent triggered when the command is received.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        User user = event.getUser();
        String userInput = null;
        List<User> userList = new ArrayList<>();
        for (OptionMapping option : event.getOptions()) {
            if (option != null && option.getAsString() != null) {
                userInput = option.getAsString();
                userList = option.getMentions().getUsers();
                break;
            }
        }
        try {
            MessageEmbed embed = EmbedBuilder.fromData(DataObject.fromJson(handleUserInformation(user, event.getChannel(), userInput, userList))).build();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
        } catch (Exception e) {
            event.getHook().sendMessage(handleUserInformation(user, event.getChannel(), userInput, userList)).setEphemeral(true).queue();
        }
    }

    /**
     * Handles user information based on the input provided.
     * 
     * @param user The user who triggered the command.
     * @param channel The message channel where the command was triggered.
     * @param userInput The input provided by the user.
     * @param mentions The mentioned users.
     * @return A string representing the response to the user.
     */
    private String handleUserInformation(User user, MessageChannel channel, String userInput, List<User> mentions) {
        if (userInput == null || userInput.isEmpty()) {
            return LangController.getString("userinfo_discord_no_argument");
        }

        long discordId;
        Optional<DIUser> userOpt = Optional.empty();
        
        try {
            discordId = Long.parseLong(userInput);
        } catch (NumberFormatException e) {
            discordId = 0;
        }
    
        if (discordId != 0) {
            userOpt = userDao.get(discordId);
        } else {
            userOpt = userDao.get(userInput);
        }
        
        if (!userOpt.isPresent() && mentions.size()>0 ) {
            userOpt = userDao.get(mentions.get(0).getIdLong());
        }
        
        if (userOpt.isPresent()) {
            long userId = userOpt.get().getPlayerDiscord().map(User::getIdLong).orElse(-1L);
            Optional<List<DIUser>> list = userDao.getList(userId);
            if (list.isPresent()) {
                return createEmbed(list.get()).toData().toString();
            }
        } else {
            return LangController.getString("userinfo_discord_no_valid");
        }
        
        return LangController.getString("userinfo_discord_no_valid");
    }


    /**
     * Sends a message to the given channel.
     * 
     * @param channel The message channel.
     * @param message The message to send.
     */
    private void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue(sentMessage -> {
            DiscordMessageDeleter.deleteMessage(20, sentMessage);
        });
    }
    
    /**
     * Sends an embed message to the given channel.
     * 
     * @param channel The message channel.
     * @param message The embed message to send.
     */
    private void sendEmbedMessage(MessageChannel channel, MessageEmbed message) {
        channel.sendMessageEmbeds(message).queue(sentMessage -> {
            DiscordMessageDeleter.deleteMessage(20, sentMessage);
        });
    }
    
    /**
     * Creates an embed containing user information.
     * 
     * @param userList The list of users to display information for.
     * @return The embedded message.
     */
    private MessageEmbed createEmbed(List<DIUser> userList) {
        StringBuilder playerNamesBuilder = new StringBuilder();
        for (int i = 0; i < userList.size(); i++) {
            DIUser user = userList.get(i);
            if (i > 0) {
                playerNamesBuilder.append(", ");
            }
            playerNamesBuilder.append(user.getPlayerName());
        }
        String formattedPlayerNames = playerNamesBuilder.toString();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LangController.getString("userinfo_discord_title"));
        embedBuilder.addField(LangController.getString("userinfo_discord_player_name"), formattedPlayerNames, false);
        if (userList.get(0).getPlayerDiscord().isPresent()) {
            User discordUser = userList.get(0).getPlayerDiscord().get();
            embedBuilder.addField(LangController.getString("userinfo_discord_id"), discordUser.getId(), false);
            embedBuilder.addField(LangController.getString("userinfo_discord_tag"), discordUser.getAsMention(), false);
            embedBuilder.setAuthor(discordUser.getName(), null, discordUser.getAvatarUrl());
        }
        embedBuilder.setTimestamp(Instant.now());
        return embedBuilder.build();
    }

    /**
     * Retrieves the alias of the command.
     * 
     * @return The alias of the command.
     */
    @Override
    public String getAlias() {
        return CommandAliasController.getAlias("userinfo_discord_command");
    }
}