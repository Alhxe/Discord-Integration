package di.dilogin.dao;

import java.util.Optional;

import di.dilogin.entity.DIUser;
import net.dv8tion.jda.api.entities.User;

/**
 * {@DIUser} DAO.
 */
public interface DIUserDao {

	/**
	 * @return Returns the user from the database.
	 */
	Optional<DIUser> get(String playerName);

	/**
	 * Add a user to the database.
	 * 
	 * @param user DIUser.
	 */
	void add(DIUser user);

	/**
	 * Delete a user from the database.
	 * 
	 * @param user DIUser.
	 */
	void remove(DIUser user);

	/**
	 * Delete a user from the database.
	 * 
	 * @param playerName Bukkit player's name.
	 */
	void remove(String playerName);

	/**
	 * @param name Bukkit player's name.
	 * @return True if player exists.
	 */
	boolean contains(String name);
	
	/**
	 * @param id Discord player's id.
	 * @return True if player exists.
	 */
	boolean containsDiscordId(long id);

	/**
	 * @param user Discord User
	 * @return How many minecraft accounts you have linked to your discord account.
	 */
	int getDiscordUserAccounts(User user);
}
