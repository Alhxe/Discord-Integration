package dilogin.minecraft.cache;

import java.util.HashMap;
import java.util.Optional;

import net.dv8tion.jda.api.entities.Message;

/**
 * Contains users who are in the process of registering / logging in
 */
public class TmpCache {

	/**
	 * Prohibits instantiation of the class.
	 */
	private TmpCache() {
		throw new IllegalStateException();
	}

	/**
	 * List of users pending login.
	 */
	private static HashMap<String, Message> loginUserList = new HashMap<>();

	/**
	 * List of users pending registration.
	 */
	private static HashMap<String, Message> registerUserList = new HashMap<>();

	/**
	 * Add a player to the pending registration list.
	 * 
	 * @param playerName Bukkit player's name.
	 */
	public static void addRegister(String playerName, Message message) {
		registerUserList.put(playerName, message);
	}

	/**
	 * Add a player to the pending login list.
	 * 
	 * @param playerName Bukkit player's name.
	 */
	public static void addLogin(String playerName, Message message) {
		loginUserList.put(playerName, message);
	}

	/**
	 * Remove a player to the pending register list.
	 * 
	 * @param playerName Bukkit player's name.
	 */
	public static void removeRegister(String playerName) {
		registerUserList.remove(playerName);
	}

	/**
	 * Remove a player to the pending login list.
	 * 
	 * @param playerName Bukkit player's name.
	 */
	public static void removeLogin(String playerName) {
		loginUserList.remove(playerName);
	}

	/**
	 * Check if there is a player on the pending registration list.
	 * 
	 * @param playerName Bukkit player's name.
	 * @return true if there is a player.
	 */
	public static boolean containsRegister(String playerName) {
		return registerUserList.containsKey(playerName);
	}

	/**
	 * Check if there is a player on the pending login list.
	 * 
	 * @param playerName Bukkit player's name.
	 * @return true if there is a player.
	 */
	public static boolean containsLogin(String playerName) {
		return loginUserList.containsKey(playerName);
	}

	/**
	 * @param playerName Bukkit player's name.
	 * @return Gets the possible message.
	 */
	public static Optional<Message> getRegisterMessage(String playerName) {
		return Optional.ofNullable(registerUserList.get(playerName));
	}

	/**
	 * @param playerName Bukkit player's name.
	 * @return Gets the possible message.
	 */
	public static Optional<Message> getLoginMessage(String playerName) {
		return Optional.ofNullable(loginUserList.get(playerName));
	}

}
