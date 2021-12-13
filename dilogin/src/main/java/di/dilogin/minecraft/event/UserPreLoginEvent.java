package di.dilogin.minecraft.event;

import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;

/**
 * Container class for user session end events.
 */
public class UserPreLoginEvent implements Listener {

	/**
	 * Main API
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		String username = event.getName();
		Server server = api.getCoreController().getPlugin().getServer();
		
		boolean isAnotherUserOnline = server.getOnlinePlayers().stream().filter(u->u.getName().equals(username)).findFirst().isPresent();
		
		if(isAnotherUserOnline)
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Your username is already logged into this server.");
		
		event.allow();
		return;
	}
}
