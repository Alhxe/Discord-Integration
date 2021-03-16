package di.dilogin.controller;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import di.dilogin.BukkitApplication;
import di.dilogin.minecraft.cache.UserBlockedCache;

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
	 * Check if the session system is enabled.
	 * 
	 * @return True if the system is active.
	 */
	public static boolean isSessionEnabled() {
		return BukkitApplication.getDIApi().getInternalController().getConfigManager().getBoolean("sessions");
	}

	/**
	 * Block a player.
	 * 
	 * @param player Bukkit player.
	 */
	public static void blockUser(Player player) {
		UserBlockedCache.add(player.getName());
	}

	/**
	 * Block a player.
	 * 
	 * @param playerName Bukkit player.
	 */
	public static void blockUser(String playerName) {
		UserBlockedCache.add(playerName);
	}

	/**
	 * Kick a player synchronously.
	 * @param player Player to kick.
	 * @param reason Reason to kick.
	 */
	public static void kickPlayer(Player player, String reason) {
		Runnable task = () -> player.kickPlayer(reason);
		Bukkit.getScheduler().runTask(BukkitApplication.getPlugin(), task);
	}

}
