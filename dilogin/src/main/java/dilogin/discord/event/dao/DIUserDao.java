package dilogin.discord.event.dao;

import dilogin.entity.DIUser;

/**
 * {@DIUser} DAO.
 */
public interface DIUserDao {

	/**
	 * @return Returns the user from the database.
	 */
	DIUser get(String playerName);

	/**
	 * Add a user to the database.
	 * 
	 * @param user DIUser.
	 */
	void add(DIUser user);

	/**
	 * Updates a user from the database.
	 * 
	 * @param user DIUser.
	 */
	void update(DIUser user);

	/**
	 * Delete a user from the database.
	 * 
	 * @param user DIUser.
	 */
	void remove(DIUser user);
	
	/**
	 * @param name Bukkit player name.
	 * @return True if player exists.
	 */
	boolean contains(String name);
}
