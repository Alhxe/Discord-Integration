package dilogin.minecraft.controller;

import org.bukkit.entity.Player;

import dilogin.minecraft.cache.UserBlockedCache;

/**
 * Login plugin control.
 */
public class LoginController {
	
	/**
	 * Prohibits instantiation of the class.
	 */
	private LoginController() {
		throw new IllegalStateException();
	}
	
	/**
	 * Block a player.
	 * @param player Bukkit player.
	 */
	public static void blockUser(Player player) {
		UserBlockedCache.add(player.getName());
	}
	
	/**
	 * Block a player.
	 * @param playerName Bukkit player.
	 */
	public static void blockUser(String playerName) {
		UserBlockedCache.add(playerName);
	}

}
