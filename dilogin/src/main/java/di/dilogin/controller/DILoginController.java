package di.dilogin.controller;

import di.dilogin.dao.DIUserDao;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

/**
 * DILogin plugin controller.
 */
public interface DILoginController {

	/**
	 *
	 * @return the user dao, class that gets data from the users.
	 */
	DIUserDao getDIUserDao();

	/**
	 * @return The basis for embed messages.
	 */
	EmbedBuilder getEmbedBase();

	/**
	 * Check if the session system is enabled.
	 *
	 * @return True if the system is active.
	 */
	boolean isSessionEnabled();

	/**
	 * Check if the rol syncro system is enabled.
	 *
	 * @return True if the system is active.
	 */
	boolean isSyncroRolEnabled();

	/**
	 * Check if syncro name option is enabled in cofig file.
	 *
	 * @return true if its enabled.
	 */
	boolean isSyncronizeOptionEnabled();

	/**
	 * @return true is Authme is enabled.
	 */
	boolean isAuthmeEnabled();

	/**
	 * @return true is nLogin is enabled.
	 */
	boolean isNLoginEnabled();

	/**
	 * @return true is LuckPerms is enabled.
	 */
	boolean isLuckPermsEnabled();

	/**
	 * Start the player session.
	 * 
	 * @param playerName Bukkit player.
	 * @param user 	 Discord user.
	 */
	void loginUser(String playerName, User user);

	/**
	 * Kick user from server.
	 *
	 * @param playerName Bukkit player.
	 */
	void kickPlayer(String playerName, String message);
}
