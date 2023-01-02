package di.dilogin.minecraft.bukkit.event.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.controller.MainController;
import di.dilogin.dto.DIUserDto;
import di.dilogin.dto.SessionDto;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.bukkit.BukkitUtil;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.internal.dto.Demand;
import di.internal.dto.converter.JsonConverter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

/**
 * Container class for user login event.
 */
public class UserLoginEjemploEventImpl implements Listener {

	/**
	 * Main join player event body.
	 * @param event Player Join Event.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		String playerIp = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString();
		UserBlockedCache.add(playerName);
		
    	JsonConverter<SessionDto> converter = new JsonConverter<SessionDto>(SessionDto.class);
    	SessionDto dto = new SessionDto();
    	dto.setPlayerName(playerName);
    	dto.setIp(playerIp);
    	String demandData = converter.getJson(dto);
    	
		MainController.getDIApi().getInternalController().getChannelController().sendMessageAndWaitResponse(event.getPlayer().getName(), Demand.getSessionStatus.name(), demandData)
        .whenCompleteAsync((s, throwable1) -> {
           SessionDto response = converter.getDto(s);
           if (response.isValid()) {
        	   TmpCache.removeLogin(playerName);
           }
        });
		
		MainController.getDIApi().getInternalController().getChannelController().sendMessageAndWaitResponse(event.getPlayer().getName(), Demand.getDIUser.name(), event.getPlayer().getName())
        .whenCompleteAsync((s, throwable1) -> {
        	// If the user is registered
    		if (userDao.contains(playerName)) {
    			initPlayerLoginRequest(event, playerName);
    		} else {
    			initPlayerRegisterRequest(event, playerName);
    		}
        });
	}

	/**
	 * Initializes the player login request.
	 * @param event      Main login event.
	 * @param playerName Player's name.
	 */
	
	public void initPlayerLoginRequest(PlayerJoinEvent event, String playerName, DIUserDto user) {
		TmpCache.addLogin(playerName, null);

		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("login_time_until_kick") * 1000;

		if (!MainController.getDiscordController().isWhiteListed(user.getPlayerName())) {
			event.getPlayer().sendMessage(LangManager.getString(user, "login_without_role_required"));
		} else {
			event.getPlayer().sendMessage(LangManager.getString(user, "login_request"));
			sendLoginMessageRequest(event.getPlayer(), user.getPlayerDiscord().get());
		}
		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the login.
			if (TmpCache.containsLogin(playerName)) {
				String message = LangManager.getString(event.getPlayer().getName(), "login_kick_time");
				MainController.getDILoginController().kickPlayer(event.getPlayer().getName(), message);
			}
			return null;
		});
	}

	/**
	 * Initializes the player register request.
	 * @param event      Main register event.
	 * @param playerName Player's name.
	 */
	public void initPlayerRegisterRequest(PlayerJoinEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
		String command = api.getCoreController().getBot().getPrefix() + api.getInternalController().getConfigManager().getString("register_command") + " " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer().getName(), null, null, code));

		int v = BukkitUtil.getServerVersion(event.getPlayer().getServer());
		
		if (v < 16)
			event.getPlayer().sendMessage(LangManager.getString(event.getPlayer().getName(), "register_request")
					.replace("%register_command%", command));

		if (v >= 16) {
			TextComponent tc = new TextComponent(LangManager.getString(event.getPlayer().getName(), "register_request")
					.replace("%register_command%", command));
			tc.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, command));
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new Text(LangManager.getString(event.getPlayer().getName(), "register_request_copy"))));
			event.getPlayer().spigot().sendMessage(tc);
		}

		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("register_time_until_kick") * 1000;

		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the registration.
			if (TmpCache.containsRegister(playerName)) {
				String message = LangManager.getString(event.getPlayer().getName(), "register_kick_time");
				MainController.getDILoginController().kickPlayer(event.getPlayer().getName(), message);
			}
			return null;
		});
	}

}
