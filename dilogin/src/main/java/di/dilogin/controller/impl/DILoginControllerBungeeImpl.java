package di.dilogin.controller.impl;

import java.time.Instant;
import java.util.Optional;

import org.bukkit.Bukkit;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.BungeeApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bukkit.event.custom.DILoginEvent;
import di.dilogin.minecraft.bungee.BungeeUtil;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.internal.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * DILogin plugin control.
 */
public class DILoginControllerBungeeImpl implements DILoginController {

	/**
	 * Starts the implementation of the class that gets data from the users.
	 */
	private static final DIUserDao userDao = new DIUserDaoSqlImpl();

	/**
	 * Get the main plugin api.
	 */
	private static final DIApi api = BungeeApplication.getDIApi();

	@Override
	public DIUserDao getDIUserDao() {
		return userDao;
	}

	@Override
	public EmbedBuilder getEmbedBase() {
		DIApi api = BungeeApplication.getDIApi();
		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(
				Util.hex2Rgb(api.getInternalController().getConfigManager().getString("discord_embed_color")));
		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_server_image")) {
			Optional<Guild> optGuild = Optional.ofNullable(api.getCoreController().getDiscordApi().get()
					.getGuildById(api.getCoreController().getConfigManager().getLong("discord_server_id")));
			if (optGuild.isPresent()) {
				String url = optGuild.get().getIconUrl();
				if (url != null)
					embedBuilder.setThumbnail(url);
			}
		}
		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_timestamp"))
			embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}

	@Override
	public boolean isSessionEnabled() {
		return BungeeApplication.getDIApi().getInternalController().getConfigManager().getBoolean("sessions");
	}

	@Override
	public boolean isSyncroRolEnabled() {
		return BungeeApplication.getDIApi().getInternalController().getConfigManager().getBoolean("syncro_rol_enable");
	}

	@Override
	public boolean isSyncronizeOptionEnabled() {
		return BungeeApplication.getDIApi().getInternalController().getConfigManager().getBoolean("syncro_enable");
	}

	@Override
	public boolean isAuthmeEnabled() {
		return BungeeApplication.getPlugin().getProxy().getPluginManager().getPlugin("AuthMe") != null;
	}

	@Override
	public boolean isNLoginEnabled() {
		return BungeeApplication.getPlugin().getProxy().getPluginManager().getPlugin("nLogin") != null;
	}

	@Override
	public boolean isLuckPermsEnabled() {
		return BungeeApplication.getPlugin().getProxy().getPluginManager().getPlugin("LuckPerms") != null
				&& api.getInternalController().getConfigManager().getBoolean("syncro_rol_enable");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void loginUser(String playerName, User user) {
		Optional<ProxiedPlayer> optionalPlayer = Optional
				.ofNullable(BungeeApplication.getPlugin().getProxy().getPlayer(playerName));

		if (!optionalPlayer.isPresent())
			return;

		ProxiedPlayer player = optionalPlayer.get();

		Bukkit.getScheduler().runTask(BukkitApplication.getPlugin(),
				() -> Bukkit.getPluginManager().callEvent(new DILoginEvent(new DIUser(playerName, Optional.of(user)))));
		UserBlockedCache.remove(player.getName());
		player.sendMessage(LangManager.getString("login_success"));

		TmpCache.removeLogin(player.getName());
	}

	@Override
	public void kickPlayer(String playerName, String message) {
		Optional<ProxiedPlayer> optionalPlayer = BungeeUtil.getProxiedPlayer(playerName);

		if (!optionalPlayer.isPresent())
			return;

		optionalPlayer.get().disconnect(new TextComponent(message));
	}
}
