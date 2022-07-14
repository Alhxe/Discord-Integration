package di.dilogin.minecraft.event;

import java.time.Duration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Contains the necessary methods for login events.
 */
public interface UserLoginEvent extends Listener {

	/**
	 * User manager.
	 */
	static final DIUserDao userDao = DILoginController.getDIUserDao();

	/**
	 * Main api.
	 */
	static final DIApi api = BukkitApplication.getDIApi();

	/**
	 * Reactions emoji.
	 */
	static final String EMOJI = api.getInternalController().getConfigManager().getString("discord_embed_emoji");
	
	/**
	 * Catch the main event when a user connects.
	 * 
	 * @param event Player Join Event.
	 */
	@EventHandler
	void onPlayerJoin(final PlayerJoinEvent event);

	/**
	 * Contains the main flow of login.
	 * 
	 * @param event      Main login event.
	 * @param playerName Player's name.
	 */
	void initPlayerLoginRequest(PlayerJoinEvent event, String playerName);

	/**
	 * Contains the main flow of register.
	 * 
	 * @param event      Main register event.
	 * @param playerName Player's name.
	 */
	void initPlayerRegisterRequest(PlayerJoinEvent event, String playerName);
	
	/**
	 * Send message to login.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 */
	default void sendLoginMessageRequest(Player player, User user) {
		MessageEmbed embed = DILoginController.getEmbedBase()
				.setTitle(LangManager.getString(player, "login_discord_title"))
				.setDescription(LangManager.getString(user, player, "login_discord_desc")).build();

		user.openPrivateChannel().submit()
				.thenAccept(channel -> channel.sendMessageEmbeds(embed).submit().thenAccept(message -> {
					message.addReaction(EMOJI).queue();
					TmpCache.addLogin(player.getName(), new TmpMessage(player, user, message, null));
				}).whenComplete((message, error) -> {
					if (error == null)
						return;

					TextChannel serverchannel = api.getCoreController().getDiscordApi()
							.getTextChannelById(api.getInternalController().getConfigManager().getLong("channel"));

					serverchannel.sendMessage(user.getAsMention()).delay(Duration.ofSeconds(10))
							.flatMap(Message::delete).queue();
					Message servermessage = serverchannel.sendMessageEmbeds(embed).submit().join();
					servermessage.addReaction(EMOJI).queue();
					TmpCache.addLogin(player.getName(), new TmpMessage(player, user, servermessage, null));
				}));
	}
}
