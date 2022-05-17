package di.dilogin.controller;

import java.time.Instant;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.AuthmeHook;
import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.event.custom.DILoginEvent;
import di.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Login plugin control.
 */
public class DILoginController {

	/**
	 * Prohibits instantiation of the class.
	 */
	private DILoginController() {
		throw new IllegalStateException();
	}

	/**
	 * 
	 */
	private static DIUserDao userDao = new DIUserDaoSqlImpl();

	public static DIUserDao getDIUserDao() {
		return userDao;
	}

	/**
	 * @return The basis for embed messages.
	 */
	public static EmbedBuilder getEmbedBase() {
		DIApi api = BukkitApplication.getDIApi();
		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(
				Utils.hex2Rgb(api.getInternalController().getConfigManager().getString("discord_embed_color")));
		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_server_image")) {
			Optional<Guild> optGuild = Optional.ofNullable(api.getCoreController().getDiscordApi()
					.getGuildById(api.getCoreController().getConfigManager().getLong("discord_server_id")));
			if (optGuild.isPresent()) {
				String url = optGuild.get().getIconUrl();
				if (url != null)
					embedBuilder.setThumbnail(url);
			}
		}
		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_timestamp"))
			embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}

	/**
	 * Kick a player synchronously.
	 * 
	 * @param player Player to kick.
	 * @param reason Reason to kick.
	 */
	public static void kickPlayer(Player player, String reason) {
		Runnable task = () -> player.kickPlayer(reason);
		Bukkit.getScheduler().runTask(BukkitApplication.getPlugin(), task);
	}

	/**
	 * Check if the session system is enabled.
	 * 
	 * @return True if the system is active.
	 */
	public static boolean isSessionEnabled() {
		return BukkitApplication.getDIApi().getInternalController().getConfigManager().getBoolean("sessions");
	}

	/**
	 * Check if the rol syncro system is enabled.
	 * 
	 * @return True if the system is active.
	 */
	public static boolean isSyncroRolEnabled() {
		return BukkitApplication.getDIApi().getInternalController().getConfigManager().getBoolean("syncro_rol_enable");
	}

	/**
	 * Check if syncro name option is enabled in cofig file.
	 * 
	 * @return true if its enabled.
	 */
	public static boolean isSyncronizeOptionEnabled() {
		return BukkitApplication.getDIApi().getInternalController().getConfigManager().getBoolean("syncro_enable");
	}

	/**
	 * @return true is Authme is enabled.
	 */
	public static boolean isAuthmeEnabled() {
		return BukkitApplication.getPlugin().getServer().getPluginManager().isPluginEnabled("AuthMe");
	}

	/**
	 * @return true is LuckPerms is enabled.
	 */
	public static boolean isLuckPermsEnabled() {
		return BukkitApplication.getPlugin().getServer().getPluginManager().isPluginEnabled("LuckPerms");
	}

	/**
	 * Start the player session.
	 * 
	 * @param player Bukkit player.
	 */
	public static void loginUser(Player player, User user) {
		if (user != null) {
			if (isSyncronizeOptionEnabled()) {
				syncroUserName(player, user);
			}
		}

		if (isAuthmeEnabled()) {
			AuthmeHook.login(player);
		} else {
			Bukkit.getScheduler().runTask(BukkitApplication.getPlugin(),
					() -> Bukkit.getPluginManager().callEvent(new DILoginEvent(player)));
			UserBlockedCache.remove(player.getName());
			player.sendMessage(LangManager.getString("login_success"));
		}
		TmpCache.removeLogin(player.getName());
	}

	/**
	 * Syncro player's name.
	 * 
	 * @param player Minecraft player.
	 */
	private static void syncroUserName(Player player, User user) {
		Optional<DIUser> optDIUser = userDao.get(player.getName());

		if (!optDIUser.isPresent())
			return;

		DIApi api = BukkitApplication.getDIApi();
		JDA jda = BukkitApplication.getDIApi().getCoreController().getDiscordApi();
		Guild guild = jda.getGuildById(api.getCoreController().getBot().getServerid());

		Member member = guild.retrieveMember(user, true).complete();
		Member bot = guild.retrieveMember(jda.getSelfUser(), true).complete();

		if (bot.canInteract(member)) {
			member.modifyNickname(player.getName()).queue();
		} else {
			api.getInternalController().getPlugin().getLogger()
					.info("Cannot change the nickname of " + player.getName() + ". Insufficient permissions.");
		}
	}

}
