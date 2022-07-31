package di.dilogin.minecraft.ext.nlogin;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nickuc.login.api.event.bukkit.command.UnregisterEvent;

import di.dilogin.controller.DILoginController;
import di.dilogin.dao.DIUserDao;

public class UnregisterNLoginEvents implements Listener{
	
	/**
	 * User management.
	 */
	private DIUserDao userDao = DILoginController.getDIUserDao();

	@EventHandler
	void onUnregisterByPlayerEvent(UnregisterEvent event) {
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
