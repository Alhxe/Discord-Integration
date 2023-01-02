package di.dilogin.minecraft.bungee.event;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;

import di.dicore.api.DIApi;
import di.dilogin.controller.LangManager;
import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.bukkit.event.custom.UserLoginEventUtils;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.cache.UserSessionCache;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@SuppressWarnings("deprecation")
public class UserLoginBungeeEvent implements Listener, UserLoginEventUtils {

	/**
	 * User manager.
	 */
	DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main api.
	 */
	DIApi api = MainController.getDIApi();

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		String playerName = event.getPlayer().getName();
		String playerIp = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString();

		// It checks if the user has a valid session
		if (MainController.getDILoginController().isSessionEnabled() && UserSessionCache.isValid(playerName, playerIp))
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

	/**
	 * Initializes the player login request.
	 * 
	 * @param event      Main login event.
	 * @param playerName Player's name.
	 */
	public void initPlayerLoginRequest(PostLoginEvent event, String playerName) {
		TmpCache.addLogin(playerName, null);
		Optional<DIUser> userOpt = userDao.get(playerName);
		if (!userOpt.isPresent())
			return;

		DIUser user = userOpt.get();

		if (!user.getPlayerDiscord().isPresent()) {
			api.getCoreController().getLogger().severe("Failed to get user in database: " + playerName);
			return;
		}

		long seconds = MainController.getDIApi().getInternalController().getConfigManager()
				.getLong("login_time_until_kick") * 1000;

		if (!MainController.getDiscordController().isWhiteListed(user.getPlayerName())) {
			event.getPlayer().sendMessage(LangManager.getString(user, "login_without_role_required"));
		} else {
			event.getPlayer().sendMessage(LangManager.getString(user, "login_request"));
			sendLoginMessageRequest(event.getPlayer().getName(), user.getPlayerDiscord().get());
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
	 * 
	 * @param event      Main register event.
	 * @param playerName Player's name.
	 */
	public void initPlayerRegisterRequest(PostLoginEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
		String command = api.getCoreController().getBot().getPrefix()
				+ api.getInternalController().getConfigManager().getString("register_command") + " " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer().getName(), null, null, code));

		TextComponent tc = new TextComponent(LangManager.getString(event.getPlayer().getName(), "register_request")
				.replace("%register_command%", command));
		tc.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, command));
		tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new Text(LangManager.getString(event.getPlayer().getName(), "register_request_copy"))));
		event.getPlayer().sendMessage(tc);

		long seconds = MainController.getDIApi().getInternalController().getConfigManager()
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