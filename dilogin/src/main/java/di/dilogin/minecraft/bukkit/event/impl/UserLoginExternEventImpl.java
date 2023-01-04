package di.dilogin.minecraft.bukkit.event.impl;

import java.util.Objects;

import di.dilogin.controller.DILoginController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.MainController;
import di.dilogin.dto.SessionDto;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.internal.dto.Demand;
import di.internal.dto.converter.JsonConverter;
import di.internal.utils.Util;

/**
 * Container class for user login event.
 */
public class UserLoginExternEventImpl implements Listener{

	/**
	 * Main join player event body.
	 * @param event Player Join Event.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!MainController.getDILoginController().isLoginSystemEnabled())
			return;

		String playerName = event.getPlayer().getName();
		String playerIp = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString();


		// We block the user while waiting for their registration or login
		UserBlockedCache.add(event.getPlayer().getName());
		
		JsonConverter<SessionDto> sessionConverter = new JsonConverter<SessionDto>(SessionDto.class);
		SessionDto session = new SessionDto();
		session.setPlayerName(playerName);
		session.setIp(playerIp);
		
		new BukkitRunnable() {
		    @Override
		    public void run() {
		    	if(!event.getPlayer().isOnline() || !UserBlockedCache.contains(playerName))
					cancel();

	            String subChannel = Util.getRandomSubChannel(playerName);
	            MainController.getDIApi().getInternalController().getChannelController().sendMessageAndWaitResponseWithSubChannel(subChannel, playerName, Demand.getSessionStatus.name(), sessionConverter.getJson(session))
	                    .whenCompleteAsync((s, throwable1) -> {
	                    	if (s==null||s.equals("error"))
	                    		return;
	                    	
	                        SessionDto response = sessionConverter.getDto(s);
	                        if (response.getPlayerName().equals(session.getPlayerName()) && response.getIp().equals(session.getIp()) && response.isValid()) {
	                        	UserBlockedCache.remove(playerName);
	                        	cancel();
	                        }
	                    });
		    }
		}.runTaskTimer(BukkitApplication.getPlugin(), 10L, 30L);
	}
}
