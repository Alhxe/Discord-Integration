package di.dilogin.minecraft.event;

import java.util.Optional;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import di.dilogin.controller.DILoginController;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.cache.UserSessionCache;
import net.dv8tion.jda.api.entities.Message;

/**
 * Container class for user session end events.
 */
public class UserLeaveEvent implements Listener {

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (TmpCache.containsRegister(event.getPlayer().getName())) {
			Optional<Message> messageOpt = TmpCache.getRegisterMessage(event.getPlayer().getName());
			if (messageOpt.isPresent()) {
				Message message = messageOpt.get();
				message.delete().queue();
				TmpCache.removeRegister(event.getPlayer().getName());
			}
		}
		if (TmpCache.containsLogin(event.getPlayer().getName())) {
			Optional<Message> messageOpt = TmpCache.getLoginMessage(event.getPlayer().getName());
			if (messageOpt.isPresent()) {
				Message message = messageOpt.get();
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
