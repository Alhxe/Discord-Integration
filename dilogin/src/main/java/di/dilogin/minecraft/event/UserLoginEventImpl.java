package di.dilogin.minecraft.event;

import java.util.Optional;
import java.util.concurrent.Executors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.cache.UserSessionCache;
import di.dilogin.minecraft.util.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class UserLoginEventImpl implements UserLoginEvent {

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		String playerIp = event.getPlayer().getAddress().getAddress().toString();

		// It checks if the user has a valid session
		if (DILoginController.isSessionEnabled() && UserSessionCache.isValid(playerName, playerIp))
			return;

		// We block the user while waiting for their registration or login
		UserBlockedCache.add(event.getPlayer().getName());

		// If the user is registered
		if (userDao.contains(playerName)) {
			initPlayerLoginRequest(event, playerName);
		} else {
			initPlayerRegisterRequest(event, playerName);
		}
	}

	@Override
	public void initPlayerLoginRequest(PlayerJoinEvent event, String playerName) {
		TmpCache.addLogin(playerName, null);
		Optional<DIUser> userOpt = userDao.get(playerName);
		if (!userOpt.isPresent())
			return;

		DIUser user = userOpt.get();
		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("login_time_until_kick") * 1000;

		if (!Util.isWhiteListed(user.getPlayerDiscord().get())) {
			event.getPlayer().sendMessage(LangManager.getString(user, "login_without_role_required"));
		} else {
			event.getPlayer().sendMessage(LangManager.getString(user, "login_request"));
			sendLoginMessageRequest(user.getPlayerBukkit().get(), user.getPlayerDiscord().get());
		}
		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the login.
			if (TmpCache.containsLogin(playerName)) {
				String message = LangManager.getString(event.getPlayer(), "login_kick_time");
				DILoginController.kickPlayer(event.getPlayer(), message);
			}
			return null;
		});
	}

	@Override
	public void initPlayerRegisterRequest(PlayerJoinEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
		String command = api.getCoreController().getBot().getPrefix() + api.getInternalController().getConfigManager().getString("register_command") + " " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer(), null, null, code));

		int v = Util.getServerVersion(api.getInternalController().getPlugin().getServer());
		
		if (v < 16)
			event.getPlayer().sendMessage(LangManager.getString(event.getPlayer(), "register_request")
					.replace("%register_command%", command));

		if (v >= 16) {
			TextComponent tc = new TextComponent(LangManager.getString(event.getPlayer(), "register_request")
					.replace("%register_command%", command));
			tc.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, command));
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new Text(LangManager.getString(event.getPlayer(), "register_request_copy"))));
			event.getPlayer().spigot().sendMessage(tc);
		}

		long seconds = BukkitApplication.getDIApi().getInternalController().getConfigManager()
				.getLong("register_time_until_kick") * 1000;

		Executors.newCachedThreadPool().submit(() -> {
			Thread.sleep(seconds);
			// In case the user has not finished completing the registration.
			if (TmpCache.containsRegister(playerName)) {
				String message = LangManager.getString(event.getPlayer(), "register_kick_time");
				DILoginController.kickPlayer(event.getPlayer(), message);
			}
			return null;
		});
	}

}
