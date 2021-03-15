package dilogin.minecraft.event;

import java.util.concurrent.Executors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dilogin.BukkitApplication;
import dilogin.dao.DIUserDao;
import dilogin.dao.DIUserDaoSqliteImpl;
import dilogin.entity.DIUser;
import dilogin.minecraft.cache.TmpCache;
import dilogin.minecraft.cache.UserSessionCache;
import dilogin.minecraft.controller.DILoginController;
import dilogin.minecraft.controller.LangManager;

/**
 * Handles events related to user login.
 */
public class UserLoginEvent implements Listener {

	/**
	 * User manager in the database.
	 */
	private DIUserDao userDao = new DIUserDaoSqliteImpl();

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		String playerIp = event.getPlayer().getAddress().getAddress().toString();

		// It checks if the user has a valid session
		if (DILoginController.isSessionEnabled() && UserSessionCache.isValid(playerName, playerIp))
			return;

		// We block the user while waiting for their registration or login
		DILoginController.blockUser(playerName);

		// If the user is registered
		if (userDao.contains(playerName)) {
			initLoginRequest(event, playerName);
		} else {
			initRegisterRequest(event, playerName);
		}
	}

	/**
	 * @param event Main login event.
	 */
	private void initLoginRequest(PlayerJoinEvent event, String playerName) {
		TmpCache.addLogin(playerName, null);
		DIUser user = userDao.get(playerName);
		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("login_time_until_kick") * 1000;

		event.getPlayer().sendMessage(LangManager.getString(user, "login_request"));

		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the login.
			if (TmpCache.containsLogin(playerName)) {

				if (TmpCache.getLoginMessage(playerName).isPresent())
					TmpCache.getLoginMessage(playerName).get().delete().submit();

				String message = LangManager.getString(event.getPlayer(), "register_kick_time");
				DILoginController.kickPlayer(event.getPlayer(), message);
			}
			return null;
		});
	}

	/**
	 * @param event Main register event.
	 */
	private void initRegisterRequest(PlayerJoinEvent event, String playerName) {
		TmpCache.addRegister(playerName, null);
		event.getPlayer().sendMessage(LangManager.getString(event.getPlayer(), "register_request"));

		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("register_time_until_kick") * 1000;

		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the registration.
			if (TmpCache.containsRegister(playerName)) {

				if (TmpCache.getRegisterMessage(playerName).isPresent())
					TmpCache.getRegisterMessage(playerName).get().delete().submit();

				String message = LangManager.getString(event.getPlayer(), "register_kick_time");
				DILoginController.kickPlayer(event.getPlayer(), message);
			}
			return null;
		});
	}

}
