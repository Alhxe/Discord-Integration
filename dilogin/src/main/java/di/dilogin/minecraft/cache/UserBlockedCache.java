package di.dilogin.minecraft.cache;

import java.util.Set;
import java.util.TreeSet;

/**
 * Contains the list of temporarily blocked users.
 */
public class UserBlockedCache {

	/**
	 * Prohibits instantiation of the class.
	 */
	private UserBlockedCache() {
		throw new IllegalStateException();
	}

	/**
	 * List of blocked users. The list contains the names of users.
	 */
	private static Set<String> blockedUsers = new TreeSet<>();

	/**
	 * Add new blocked user.
	 * 
	 * @param playerName Bukkit Player's name.
	 */
	public static void add(String playerName) {
		blockedUsers.add(playerName);
	}

	/**
	 * Remove blocked user.
	 * 
	 * @param playerName Bukkit Player's name.
	 */
	public static void remove(String playerName) {
		blockedUsers.remove(playerName);
	}

	/**
	 * @param playerName Bukkit Player's name.
	 * @return True if player is blocked.
	 */
	public static boolean contains(String playerName) {
		return blockedUsers.contains(playerName);
	}

}
