package di.dilogin.minecraft.bungee.event;

import java.util.Objects;
import java.util.Optional;

import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.cache.UserSessionCache;
import net.dv8tion.jda.api.entities.Message;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class UserLeaveBungeeEvent implements Listener{
	
	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerLeft(PlayerDisconnectEvent event) {
		boolean session = MainController.getDILoginController().isSessionEnabled();
		boolean isInRegister = TmpCache.containsRegister(event.getPlayer().getName());
		boolean isInLogin = TmpCache.containsLogin(event.getPlayer().getName());
		boolean isUserRegistered = userDao.contains(event.getPlayer().getName());

		// Check if add session
		if (session && !isInRegister && !isInLogin && isUserRegistered) {
			UserSessionCache.addSession(event.getPlayer().getName(),
					Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString());
		}

		if (isInRegister) {
			inRegister(event);
		}
		if (isInLogin) {
			inLogin(event);
		}

		if (UserBlockedCache.contains(event.getPlayer().getName())) {
			UserBlockedCache.remove(event.getPlayer().getName());
		} else if (MainController.getDILoginController().isSessionEnabled()) {
			UserSessionCache.addSession(event.getPlayer().getName(),
					Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString());
		}
	}
	
	/**
	 * Runs when a user is logged in.
	 * @param event It is the object that includes the event information.
	 */
	private void inLogin(PlayerDisconnectEvent event) {
		Optional<TmpMessage> messageOpt = TmpCache.getLoginMessage(event.getPlayer().getName());
		if (messageOpt.isPresent()) {
			Message message = messageOpt.get().getMessage();
			if (message != null)
				message.delete().queue();
			TmpCache.removeLogin(event.getPlayer().getName());
		}
	}

	/**
	 * Runs when a user is registering.
	 * @param event It is the object that includes the event information.
	 */
	private void inRegister(PlayerDisconnectEvent event) {
		Optional<TmpMessage> messageOpt = TmpCache.getRegisterMessage(event.getPlayer().getName());
		if (messageOpt.isPresent()) {
			Message message = messageOpt.get().getMessage();
			if (message != null)
				message.delete().queue();
			TmpCache.removeRegister(event.getPlayer().getName());
		}
	}

}
