package di.dilogin.minecraft.bukkit.ext.nlogin;

import java.util.Optional;

import di.dilogin.controller.MainController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nickuc.login.api.event.bukkit.command.UnregisterEvent;

import di.dilogin.dao.DIUserDao;

/**
 * Unregister from nLogin plugin.
 */
public class UnregisterNLoginEvents implements Listener{
	
	/**
	 * User management.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Unregister nLogin event.
	 * @param event Unregister event.
	 */
	@EventHandler
	void onUnregisterByPlayerEvent(UnregisterEvent event) {
		Optional<Player> optPlayer = Optional.ofNullable(event.getPlayer());
		optPlayer.ifPresent(player -> unregister(player.getName()));
	}

	/**
	 * Unregister user from DILogin.
	 */
	private void unregister(String playerName) {
		userDao.remove(playerName);
	}
}
