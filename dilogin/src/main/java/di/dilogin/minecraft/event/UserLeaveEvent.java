package di.dilogin.minecraft.event;

import java.util.Optional;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import di.dilogin.controller.DILoginController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.cache.UserSessionCache;
import net.dv8tion.jda.api.entities.Message;

/**
 * Container class for user session end events.
 */
public class UserLeaveEvent implements Listener {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = DILoginController.getDIUserDao();

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		boolean session = DILoginController.isSessionEnabled();
		boolean isInRegister = TmpCache.containsRegister(event.getPlayer().getName());
		boolean isInLogin = TmpCache.containsLogin(event.getPlayer().getName());
		boolean isUserRegistered = userDao.contains(event.getPlayer().getName());

		// Check if add session
		if (session && !isInRegister && !isInLogin && isUserRegistered) {
			UserSessionCache.addSession(event.getPlayer().getName(),
					event.getPlayer().getAddress().getAddress().toString());
		}

		if (isInRegister) {
			Optional<TmpMessage> messageOpt = TmpCache.getRegisterMessage(event.getPlayer().getName());
			if (messageOpt.isPresent()) {
				Message message = messageOpt.get().getMessage();
				if (message != null)
					message.delete().queue();
				TmpCache.removeRegister(event.getPlayer().getName());
			}
		}
		if (isInLogin) {
			Optional<TmpMessage> messageOpt = TmpCache.getLoginMessage(event.getPlayer().getName());
			if (messageOpt.isPresent()) {
				Message message = messageOpt.get().getMessage();
				if (message != null)
					message.delete().queue();
				TmpCache.removeLogin(event.getPlayer().getName());
			}
		}

		if (UserBlockedCache.contains(event.getPlayer().getName())) {
			UserBlockedCache.remove(event.getPlayer().getName());
		} else if (DILoginController.isSessionEnabled()) {
			UserSessionCache.addSession(event.getPlayer().getName(),
					event.getPlayer().getAddress().getAddress().toString());
		}
	}
}
