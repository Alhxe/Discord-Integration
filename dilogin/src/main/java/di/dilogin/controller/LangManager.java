package di.dilogin.controller;

import org.bukkit.entity.Player;

import di.dilogin.BukkitApplication;
import di.dilogin.entity.DIUser;
import net.dv8tion.jda.api.entities.User;

/**
 * This class transforms the elements received from the lang file into
 * variables. (Placeholders)
 */
public class LangManager {

	/**
	 * Prohibits instantiation of the class.
	 */
	private LangManager() {
		throw new IllegalStateException();
	}

	/**
	 * @param path Variable searched.
	 * @return Original message.
	 */
	public static String getString(String path) {
		return BukkitApplication.getDIApi().getInternalController().getLangManager().getString(path).replace(
				"%minecraft_servername%",
				BukkitApplication.getDIApi().getCoreController().getConfigManager().getString("server_name"));
	}
	
	/**
	 * @param nick Bukkit player name.
	 * @param path   Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(String nick, String path) {
		return getString(path).replace("%minecraft_username%", nick);
	}

	/**
	 * @param player Bukkit player.
	 * @param path   Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(Player player, String path) {
		return getString(path).replace("%minecraft_username%", player.getName());
	}

	/**
	 * @param user
	 * @param player Bukkit player.
	 * @param path   Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(DIUser user, String path) {
		return getString(user.getPlayerBukkit().get(), path).replace("%discriminated_discord_name%",
				String.valueOf(user.getPlayerDiscord().getName()) + "#" + user.getPlayerDiscord().getDiscriminator());
	}

	/**
	 * @param user   Discord user.
	 * @param player Bukkit player.
	 * @param path   Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(User user, Player player, String path) {
		return getString(player, path).replace("%discriminated_discord_name%",
				String.valueOf(user.getName()) + "#" + user.getDiscriminator());
	}
}
