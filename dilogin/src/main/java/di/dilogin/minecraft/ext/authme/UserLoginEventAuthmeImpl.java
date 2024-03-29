package di.dilogin.minecraft.ext.authme;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.bukkit.event.UserLoginEvent;
import di.dilogin.minecraft.bukkit.event.custom.DILoginEvent;
import di.dilogin.minecraft.cache.TmpCache;
import fr.xephi.authme.events.LoginEvent;

/**
 * Implementation of user login event to Authme.
 */
public class UserLoginEventAuthmeImpl implements UserLoginEvent {

	/**
	 * Contains the main flow of login.
	 *
	 * @param event Main login event.
	 */
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

	/**
	 * Send login request to the user.
	 *
	 * @param event      Main login event.
	 * @param playerName Player's name.
	 */
	@Override
	public void initPlayerLoginRequest(PlayerJoinEvent event, String playerName) {
		TmpCache.addLogin(playerName, null);
		Optional<DIUser> userOpt = userDao.get(playerName);
		if (!userOpt.isPresent())
			return;

		DIUser user = userOpt.get();

		if (!user.getPlayerDiscord().isPresent()){
			api.getCoreController().getLogger().severe("Failed to get user in database: "+playerName);
			return;
		}

		event.getPlayer().sendMessage(LangController.getString(user, "login_request"));
		sendLoginMessageRequest(event.getPlayer().getName(), user.getPlayerDiscord().get());
	}

	/**
	 * Send register request to the user.
	 *
	 * @param event      Main register event.
	 * @param playerName Player's name.
	 */
	@Override
	public void initPlayerRegisterRequest(PlayerJoinEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer().getName(), null, null, code));
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

		Optional<DIUser> diUserOptional = userDao.get(playerName);

		if (!diUserOptional.isPresent())
			return;

		Bukkit.getScheduler().runTask(BukkitApplication.getPlugin(),
				() -> Bukkit.getPluginManager().callEvent(new DILoginEvent(diUserOptional.get())));
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
		String command = api.getCoreController().getBot().getPrefix() + CommandAliasController.getAlias("register_command") + " " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer().getName(), null, null, code));
		event.getPlayer().sendMessage(LangController.getString(event.getPlayer().getName(), "register_opt_request")
				.replace("%register_command%", command));
	}
}
