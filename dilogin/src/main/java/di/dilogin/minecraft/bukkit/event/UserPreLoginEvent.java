package di.dilogin.minecraft.bukkit.event;

import java.util.Optional;

import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;

/**
 * Container class for user session end events.
 */
public class UserPreLoginEvent implements Listener {

	/**
	 * Main API
	 */
	private final DIApi api = MainController.getDIApi();

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main event body.
	 * @param event It is the object that includes the event information.
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		String username = event.getName();
		Server server = BukkitApplication.getPlugin().getServer();

		boolean isAnotherUserOnline = server.getOnlinePlayers().stream().anyMatch(u -> u.getName().equals(username));

		if (isAnotherUserOnline) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					api.getInternalController().getLangManager().getString("login_not_authorized"));
			return;
		}

		if (!checkRegisteredUser(username)) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					api.getInternalController().getLangManager().getString("login_user_not_discord"));
		}
	}

	/**
	 * Check if a logged in Minecraft user is still present on the server.
	 * 
	 * @param username User login.
	 * @return true if user is registered and present on discord.
	 */
	private boolean checkRegisteredUser(String username) {
		boolean isUserRegistered = userDao.contains(username);
		if (isUserRegistered) {
			Optional<DIUser> userOpt = userDao.get(username);

			if (userOpt.isPresent()) {
				DIUser user = userOpt.get();
				return user.getPlayerDiscord().isPresent();
			}
		}
		return true;
	}
}
