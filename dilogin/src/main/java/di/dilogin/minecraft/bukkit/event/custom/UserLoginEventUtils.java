package di.dilogin.minecraft.bukkit.event.custom;

import java.time.Duration;

import di.dicore.api.DIApi;
import di.dilogin.controller.LangManager;
import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

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
	 * @param player Minecraft player name.
	 * @param user   Discord user.
	 */
	default void sendLoginMessageRequest(String playerName, User user) {
		MessageEmbed embed = MainController.getDILoginController().getEmbedBase()
				.setTitle(LangManager.getString(playerName, "login_discord_title"))
				.setDescription(LangManager.getString(user, playerName, "login_discord_desc")).build();

		boolean hasMessagesOnlyChannel = api.getInternalController().getConfigManager()
				.contains("messages_only_channel");

		if (hasMessagesOnlyChannel)
			hasMessagesOnlyChannel = api.getInternalController().getConfigManager().getBoolean("messages_only_channel");

		if (hasMessagesOnlyChannel) {
			sendServerMessage(user, playerName, embed);
		} else {
			user.openPrivateChannel().submit()
					.thenAccept(channel -> channel.sendMessageEmbeds(embed).submit().thenAccept(message -> {
						message.addReaction(EMOJI).queue();
						TmpCache.addLogin(playerName, new TmpMessage(playerName, user, message, null));
					}).whenComplete((message, error) -> {
						if (error == null)
							return;

						sendServerMessage(user, playerName, embed);
					}));
		}
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
