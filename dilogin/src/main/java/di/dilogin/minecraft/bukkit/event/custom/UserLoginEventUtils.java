package di.dilogin.minecraft.bukkit.event.custom;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import di.dicore.api.DIApi;
import di.dilogin.controller.MainController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Class that implements useful methods for login events.
 */
public interface UserLoginEventUtils {
	
	/**
	 * User manager.
	 */
	DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main api.
	 */
	DIApi api = MainController.getDIApi();
	
	/**
	 * Reactions emoji.
	 */
	String EMOJI = api.getInternalController().getConfigManager().getString("discord_embed_emoji");
	
	/**
	 * Send message to login.
	 * 
	 * @param playerName Minecraft player name.
	 * @param user   Discord user.
	 */
	default void sendLoginMessageRequest(String playerName, User user) {
	    MessageEmbed embed = MainController.getDILoginController().getEmbedBase()
	            .setTitle(LangController.getString(playerName, "login_discord_title"))
	            .setDescription(LangController.getString(user, playerName, "login_discord_desc")).build();

	    boolean hasMessagesOnlyChannel = api.getInternalController().getConfigManager()
	            .contains("messages_only_channel");

	    if (hasMessagesOnlyChannel)
	        hasMessagesOnlyChannel = api.getInternalController().getConfigManager().getBoolean("messages_only_channel");

	    if (hasMessagesOnlyChannel) {
	        sendServerMessage(user, playerName, embed);
	    } else {
	        user.openPrivateChannel().submit()
	                .thenAccept(channel -> sendMessage(channel, embed, playerName, user))
	                .exceptionally(e -> {
	                    MainController.getDIApi().getInternalController().getLogger().severe(e.toString());
	                    sendServerMessage(user, playerName, embed);
	                    return null;
	                });
	    }
	}

	/**
	 * Send message using a PrivateChannel.
	 * 
	 * @param channel Discord PrivateChannel.
	 * @param embed Embed message.
	 * @param playerName Minecraft player name.
	 * @param user Discord user.
	 */
	default void sendMessage(PrivateChannel channel, MessageEmbed embed, String playerName, User user) {
	    CompletableFuture<Message> sendMessageFuture = channel.sendMessageEmbeds(embed).submit();
	    sendMessageFuture.thenAccept(message -> {
	        message.addReaction(EMOJI).queue();
	        TmpCache.addLogin(playerName, new TmpMessage(playerName, user, message, null));
	    });
	}

	/**
	 * Send embed message to the main discord channel.
	 * 
	 * @param player Minecraft player name.
	 * @param user   Discord user.
	 * @param embed  Embed message.
	 */
	default void sendServerMessage(User user, String playerName, MessageEmbed embed) {
		TextChannel serverchannel = api.getCoreController().getDiscordApi().get()
				.getTextChannelById(api.getInternalController().getConfigManager().getLong("channel"));

		serverchannel.sendMessage(user.getAsMention()).delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
		Message servermessage = serverchannel.sendMessageEmbeds(embed).submit().join();
		servermessage.addReaction(EMOJI).queue();
		TmpCache.addLogin(playerName, new TmpMessage(playerName, user, servermessage, null));
	}
}
