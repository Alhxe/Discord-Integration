package di.dilogin.controller;

public interface DiscordController {
	
	/**
	 * @param roleid Discord role id.
	 * @param player Bukkit player name.
	 * @return True if the user has the role.
	 */
	public boolean userHasRole(String roleid, String player);

	/**
	 * @param roleid Discord role id.
	 * @return True if the server contains the requested role.
	 */
	public boolean serverHasRole(String roleid);
	
	/**
	 * Give a role to a discord user.
	 *
	 * @param roleid Role id.
	 * @param player Bukkit player name.
	 * @param reason Reason for giving a role.
	 */
	public void giveRole(String roleid, String player, String reason);
	
	/**
	 * Remove a role to a discord user.
	 *
	 * @param roleid Role id.
	 * @param player Bukkit player name.
	 * @param reason Reason for removing a role.
	 */
	public void removeRole(String roleid, String player, String reason);
	
    /**
     * Check if user is whitelisted and option is enabled.
     *
     * @param player Player to check.
     * @return true if is whitelisted.
     */
    public boolean isWhiteListed(String player);
	
}
