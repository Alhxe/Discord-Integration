package di.dilogin.minecraft.cache;

import java.util.HashMap;
import java.util.Optional;

import di.dilogin.entity.TmpMessage;

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
	private static final HashMap<String, TmpMessage> loginUserList = new HashMap<>();

	/**
	 * List of users pending registration.
	 */
	private static final HashMap<String, TmpMessage> registerUserList = new HashMap<>();

	/**
	 * Add a player to the pending registration list.
	 * 
	 * @param playerName Bukkit player's name.
	 */
	public static void addRegister(String playerName, TmpMessage message) {
		registerUserList.put(playerName, message);
	}

	/**
	 * Add a player to the pending login list.
	 * 
	 * @param playerName Bukkit player's name.
	 */
	public static void addLogin(String playerName, TmpMessage message) {
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
	public static Optional<TmpMessage> getRegisterMessage(String playerName) {
		return Optional.ofNullable(registerUserList.get(playerName));
	}

	/**
	 * @param playerName Bukkit player's name.
	 * @return Gets the possible message.
	 */
	public static Optional<TmpMessage> getLoginMessage(String playerName) {
		return Optional.ofNullable(loginUserList.get(playerName));
	}

	/**
	 * @param id Message id sent in the registration request.
	 * @return Possible register message.
	 */
	public static Optional<TmpMessage> getRegisterMessage(long id) {
		try {
			return registerUserList.values().stream().filter(tmpMessage -> tmpMessage.getMessage().getIdLong() == id)
					.findFirst();
		} catch (NullPointerException e) {
			return Optional.empty();
		}
	}

	/**
	 * @param id Message id sent in the login request.
	 * @return Possible login message.
	 */
	public static Optional<TmpMessage> getLoginMessage(long id) {
		try {
			return loginUserList.values().stream().filter(tmpMessage -> tmpMessage.getMessage().getIdLong() == id)
					.findFirst();
		} catch (NullPointerException e) {
			return Optional.empty();
		}
	}

	/**
	 * @param code Code registration.
	 * @return Possible register message.
	 */
	public static Optional<TmpMessage> getRegisterMessageByCode(String code) {
		try {
			return registerUserList.values().stream().filter(tmpMessage -> tmpMessage.getCode().equals(code))
					.findFirst();
		} catch (NullPointerException e) {
			return Optional.empty();
		}
	}

	/**
	 * Delete all messages.
	 */
	public static void clearAll() {
		loginUserList.values().forEach(tmpMessage -> tmpMessage.getMessage().delete().queue());
		loginUserList.clear();
		registerUserList.values().forEach(tmpMessage -> tmpMessage.getMessage().delete().queue());
		registerUserList.clear();
	}

}
