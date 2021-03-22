package di.dilogin.controller;

import java.time.Instant;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.entity.AuthmeHook;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

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
	 * Check if the session system is enabled.
	 * 
	 * @return True if the system is active.
	 */
	public static boolean isSessionEnabled() {
		return BukkitApplication.getDIApi().getInternalController().getConfigManager().getBoolean("sessions");
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
	
	public static boolean isAuthmeEnabled() {
		return BukkitApplication.getPlugin().getServer().getPluginManager().isPluginEnabled("AuthMe");
	}

	/**
	 * Start the player session.
	 * 
	 * @param player Bukkit player.
	 */
	public static void loginUser(Player player) {
		if (isAuthmeEnabled()) {
			AuthmeHook.login(player);
		} else {
			UserBlockedCache.remove(player.getName());
			player.sendMessage(LangManager.getString("login_success"));
		}
		TmpCache.removeLogin(player.getName());
	}

}
