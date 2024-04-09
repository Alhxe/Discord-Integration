package di.dilogin.dao;

import java.util.List;
import java.util.Optional;

import di.dilogin.entity.DIUser;

/**
 * {@DIUser} DAO.
 */
public interface DIUserDao {

	/**
	 * @param playerName Bukkit player's name.
	 * @return Returns the user from the database.
	 */
	Optional<DIUser> get(String playerName);
	
	/**
	 * @param discord Discord user id.
	 * @return Returns the user from the database.
	 */
	Optional<DIUser> get(long discordid);
	
	/**
	 * @param discord Discord user id.
	 * @return Returns the user list from the database.
	 */
	Optional<List<DIUser>> getList(long discordid);

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
	 * @param discordId Discord User id.
	 * @return How many minecraft accounts you have linked to your discord account.
	 */
	int getDiscordUserAccounts(long discordId);
	
	/**
	 * 
	 * @return All users registered
	 */
	List<DIUser> getAllUsers();
}
