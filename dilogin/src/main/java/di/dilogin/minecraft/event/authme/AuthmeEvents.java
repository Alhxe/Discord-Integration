package di.dilogin.minecraft.event.authme;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import fr.xephi.authme.events.UnregisterByAdminEvent;
import fr.xephi.authme.events.UnregisterByPlayerEvent;

/**
 * AuthMe related events.
 */
public class AuthmeEvents implements Listener {

	/**
	 * User management.
	 */
	private DIUserDao userDao = new DIUserDaoSqlImpl();

	@EventHandler
	void onUnregisterByAdminEvent(UnregisterByAdminEvent event) {
		unregister(event.getPlayerName());
	}

	@EventHandler
	void onUnregisterByPlayerEvent(UnregisterByPlayerEvent event) {
		Optional<Player> optPlayer = Optional.ofNullable(event.getPlayer());
		if (optPlayer.isPresent())
			unregister(optPlayer.get().getName());
	}

	/**
	 * Unregister user from DILogin.
	 */
	private void unregister(String playerName) {
		userDao.remove(playerName);
	}
}
