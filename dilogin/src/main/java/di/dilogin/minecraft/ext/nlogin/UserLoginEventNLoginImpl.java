package di.dilogin.minecraft.ext.nlogin;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.nickuc.login.api.event.bukkit.auth.AuthenticateEvent;

import di.dilogin.controller.LangManager;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.event.UserLoginEvent;
import di.dilogin.minecraft.event.custom.DILoginEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UserLoginEventNLoginImpl implements UserLoginEvent {

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		if (userDao.contains(playerName))
			initPlayerLoginRequest(event, playerName);
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
		// Can't register from nLogin with DILogin
	}

	/**
	 * Event when a user logs in with nLogin.
	 * 
	 * @param event LoginEvent.
	 */
	@EventHandler
	public void onAuth(final AuthenticateEvent event) {
		String playerName = event.getPlayer().getName();

		if (!userDao.contains(playerName)) {
			initPlayerNLoginRegisterRequest(event, playerName);
		}

		Bukkit.getScheduler().runTask(api.getInternalController().getPlugin(),
				() -> Bukkit.getPluginManager().callEvent(new DILoginEvent(event.getPlayer())));
	}

	/**
	 * If the user logged in with nLogin is not registered with DILogin, it prompts
	 * him to register.
	 * 
	 * @param event      nLogin login event.
	 * @param playerName Player's name.
	 */
	@SuppressWarnings("deprecation")
	public void initPlayerNLoginRegisterRequest(AuthenticateEvent event, String playerName) {
		String code = CodeGenerator
				.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
		String command = api.getCoreController().getBot().getPrefix() + api.getInternalController().getConfigManager().getString("register_command") + " " + code;
		TmpCache.addRegister(playerName, new TmpMessage(event.getPlayer(), null, null, code));
		TextComponent message = new TextComponent(LangManager.getString(event.getPlayer(), "register_opt_request")
				.replace("%register_command%", command));
		message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code));
		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Copy").create()));
		event.getPlayer().spigot().sendMessage(message);
	}
}
