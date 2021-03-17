package di.dilogin.minecraft.event;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;

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
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.cache.UserSessionCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Handles events related to user login.
 */
public class UserLoginEvent implements Listener {

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
		String playerIp = event.getPlayer().getAddress().getAddress().toString();

		// It checks if the user has a valid session
		if (DILoginController.isSessionEnabled() && UserSessionCache.isValid(playerName, playerIp))
			return;

		// We block the user while waiting for their registration or login
		UserBlockedCache.add(event.getPlayer().getName());

		// If the user is registered
		if (userDao.contains(playerName)) {
			initLoginRequest(event, playerName);
		} else {
			initRegisterRequest(event, playerName);
		}
	}

	/**
	 * @param event Main login event.
	 */
	private void initLoginRequest(PlayerJoinEvent event, String playerName) {
		TmpCache.addLogin(playerName, null);
		Optional<DIUser> userOpt = userDao.get(playerName);
		if (!userOpt.isPresent())
			return;

		DIUser user = userOpt.get();
		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("login_time_until_kick") * 1000;

		event.getPlayer().sendMessage(LangManager.getString(user, "login_request"));
		sendLoginMessageRequest(user.getPlayerBukkit(), user.getPlayerDiscord());

		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the login.
			if (TmpCache.containsLogin(playerName)) {
				String message = LangManager.getString(event.getPlayer(), "login_kick_time");
				DILoginController.kickPlayer(event.getPlayer(), message);
			}
			return null;
		});
	}

	/**
	 * @param event Main register event.
	 */
	private void initRegisterRequest(PlayerJoinEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"));
		String command = api.getCoreController().getBot().getPrefix() + "register " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer(),null,null,code));
		event.getPlayer().sendMessage(
				LangManager.getString(event.getPlayer(), "register_request").replace("%register_command%", command));

		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("register_time_until_kick") * 1000;

		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the registration.
			if (TmpCache.containsRegister(playerName)) {
				String message = LangManager.getString(event.getPlayer(), "register_kick_time");
				DILoginController.kickPlayer(event.getPlayer(), message);
			}
			return null;
		});
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

/**
 * Code generator for registration.
 */
class CodeGenerator {

	/**
	 * Prohibits instantiation of the class.
	 */
	private CodeGenerator() {
		throw new IllegalStateException();
	}

	/**
	 * List of valid numbers for the code.
	 */
	private static final String NUMBERS = "0123456789";

	/**
	 * List of valid uppercase letters for the code.
	 */
	private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * List of valid lowercase letters for the code.
	 */
	private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";

	/**
	 * @param length Number of characters for the code.
	 * @return Generated code.
	 */
	public static final String getCode(int length) {
		return getCode(NUMBERS + CAPITAL_LETTERS + LOWER_CASE, length);
	}

	/**
	 * Generate the code.
	 * 
	 * @param key    Total characters for code generation.
	 * @param length Size that the code will have.
	 * @return Generated code.
	 */
	private static String getCode(String key, int length) {
		String code = "";
		for (int i = 0; i < length; i++) {
			code += (key.charAt((int) (Math.random() * key.length())));
		}
		return code;
	}
}
