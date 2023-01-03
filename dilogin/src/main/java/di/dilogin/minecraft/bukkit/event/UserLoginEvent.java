package di.dilogin.minecraft.bukkit.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dilogin.minecraft.bukkit.event.custom.UserLoginEventUtils;

/**
 * Contains the necessary methods for login events.
 */
public interface UserLoginEvent  extends Listener, UserLoginEventUtils{

	/**
	 * Catch the main event when a user connects.
	 * 
	 * @param event Player Join Event.
	 */
	@EventHandler
	void onPlayerJoin(final PlayerJoinEvent event);

	/**
	 * Contains the main flow of login.
	 * 
	 * @param event      Main login event.
	 * @param playerName Player's name.
	 */
	void initPlayerLoginRequest(PlayerJoinEvent event, String playerName);

	/**
	 * Contains the main flow of register.
	 * 
	 * @param event      Main register event.
	 * @param playerName Player's name.
	 */
	void initPlayerRegisterRequest(PlayerJoinEvent event, String playerName);
}
