package di.dilogin.minecraft.event;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqliteImpl;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import fr.xephi.authme.events.LoginEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Handles events related to user login.
 */
public class UserLoginAuthMeEvent implements Listener {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = new DIUserDaoSqliteImpl();

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	/**
	 * Reactions emoji.
	 */
	private final String emoji = api.getInternalController().getConfigManager().getString("discord_embed_emoji");

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		if (!userDao.contains(playerName)) {
			String code = CodeGenerator
					.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"));
			TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer(), null, null, code));
		} else {
			initLoginRequest(event, playerName);
		}
	}

	@EventHandler
	public void onAuth(final LoginEvent event) {

		String playerName = event.getPlayer().getName();

		// If the user is registered
		if (!userDao.contains(playerName)) {
			initRegisterRequest(event, playerName);
		}
	}

	/**
	 * @param event Main login event.
	 * @param playerName Player's name.
	 */
	private void initLoginRequest(PlayerJoinEvent event, String playerName) {
		TmpCache.addLogin(playerName, null);
		Optional<DIUser> userOpt = userDao.get(playerName);
		if (!userOpt.isPresent())
			return;

		DIUser user = userOpt.get();

		event.getPlayer().sendMessage(LangManager.getString(user, "login_request"));
		sendLoginMessageRequest(user.getPlayerBukkit(), user.getPlayerDiscord());
	}

	/**
	 * @param event Main register event.
	 * @param playerName player's name.
	 */
	private void initRegisterRequest(LoginEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"));
		String command = api.getCoreController().getBot().getPrefix() + "register " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer(), null, null, code));
		event.getPlayer().sendMessage(
				LangManager.getString(event.getPlayer(), "register_opt_request").replace("%register_command%", command));
	}

	/**
	 * Send message to login.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 */
	private void sendLoginMessageRequest(Player player, User user) {
		MessageEmbed embed = DILoginController.getEmbedBase()
				.setTitle(LangManager.getString(player, "login_discord_title"))
				.setDescription(LangManager.getString(user, player, "login_discord_desc")).build();

		user.openPrivateChannel().submit()
				.thenAccept(channel -> channel.sendMessage(embed).submit().thenAccept(message -> {
					message.addReaction(emoji).queue();
					TmpCache.addLogin(player.getName(), new TmpMessage(player, user, message, null));
				}).whenComplete((message, error) -> {
					if (error == null)
						return;

					TextChannel serverchannel = api.getCoreController().getDiscordApi()
							.getTextChannelById(api.getInternalController().getConfigManager().getLong("channel"));

					serverchannel.sendMessage(user.getAsMention()).delay(Duration.ofSeconds(10))
							.flatMap(Message::delete).queue();
					Message servermessage = serverchannel.sendMessage(embed).submit().join();
					servermessage.addReaction(emoji).queue();
					TmpCache.addLogin(player.getName(), new TmpMessage(player, user, servermessage, null));
				}));
	}

}
