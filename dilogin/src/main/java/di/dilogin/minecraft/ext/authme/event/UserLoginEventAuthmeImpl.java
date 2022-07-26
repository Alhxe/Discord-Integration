package di.dilogin.minecraft.ext.authme.event;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dilogin.controller.LangManager;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.event.UserLoginEvent;
import di.dilogin.minecraft.event.custom.DILoginEvent;
import fr.xephi.authme.events.LoginEvent;

public class UserLoginEventAuthmeImpl implements UserLoginEvent {

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		if (!userDao.contains(playerName)) {
			initPlayerRegisterRequest(event, playerName);
		} else {
			initPlayerLoginRequest(event, playerName);
		}
	}

	@Override
	public void initPlayerLoginRequest(PlayerJoinEvent event, String playerName) {
		TmpCache.addLogin(playerName, null);
		Optional<DIUser> userOpt = userDao.get(playerName);
		if (!userOpt.isPresent())
			return;

		DIUser user = userOpt.get();

		event.getPlayer().sendMessage(LangManager.getString(user, "login_request"));
		sendLoginMessageRequest(user.getPlayerBukkit().get(), user.getPlayerDiscord().get());
	}

	@Override
	public void initPlayerRegisterRequest(PlayerJoinEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer(), null, null, code));
	}

	/**
	 * Event when a user logs in with authme.
	 * 
	 * @param event LoginEvent.
	 */
	@EventHandler
	public void onAuth(final LoginEvent event) {
		String playerName = event.getPlayer().getName();

		if (!userDao.contains(playerName)) {
			initPlayerAuthmeRegisterRequest(event, playerName);
		}

		Bukkit.getScheduler().runTask(api.getInternalController().getPlugin(),
				() -> Bukkit.getPluginManager().callEvent(new DILoginEvent(event.getPlayer())));
	}

	/**
	 * If the user logged in with Authme is not registered with DILogin, it prompts
	 * him to register.
	 * 
	 * @param event      Authme login event.
	 * @param playerName Player's name.
	 */
	public void initPlayerAuthmeRegisterRequest(LoginEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
		String command = api.getCoreController().getBot().getPrefix() + api.getInternalController().getConfigManager().getString("register_command") + " " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer(), null, null, code));
		event.getPlayer().sendMessage(LangManager.getString(event.getPlayer(), "register_opt_request")
				.replace("%register_command%", command));
	}
}
