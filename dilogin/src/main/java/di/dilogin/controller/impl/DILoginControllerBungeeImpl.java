package di.dilogin.controller.impl;

import java.time.Instant;
import java.util.Optional;

import di.dicore.api.DIApi;
import di.dilogin.BungeeApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.MainController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bukkit.event.custom.DILoginBungeeEvent;
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
 * {@DILoginController} implementation for Bungee Proxy.
 */
public class DILoginControllerBungeeImpl implements DILoginController {

	/**
	 * Starts the implementation of the class that gets data from the users.
	 */
	private static final DIUserDao userDao = new DIUserDaoSqlImpl();

	/**
	 * Get the main plugin api.
	 */
	private static final DIApi api = MainController.getDIApi();

	@Override
	public DIUserDao getDIUserDao() {
		return userDao;
	}

	@Override
	public EmbedBuilder getEmbedBase() {
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

	@SuppressWarnings("deprecation")
	@Override
	public void loginUser(String playerName, User user) {
		Optional<ProxiedPlayer> optionalPlayer = BungeeUtil.getProxiedPlayer(playerName);

		if (!optionalPlayer.isPresent())
			return;

		ProxiedPlayer player = optionalPlayer.get();

		UserBlockedCache.remove(player.getName());
		player.sendMessage(LangController.getString("login_success"));

		BungeeApplication.getPlugin().getProxy().getScheduler().runAsync(BungeeApplication.getPlugin(),
				() -> BungeeApplication.getPlugin().getProxy().getPluginManager()
						.callEvent(new DILoginBungeeEvent(new DIUser(playerName, Optional.ofNullable(user)))));

		TmpCache.removeLogin(player.getName());

		if (MainController.getDIApi().getInternalController().getConfigManager()
				.getBoolean("teleport_server_enabled")) {
			String serverName = MainController.getDIApi().getInternalController().getConfigManager()
					.getString("teleport_server_name");
			player.connect(BungeeApplication.getPlugin().getProxy().getServerInfo(serverName));
		}
	}

	@Override
	public void kickPlayer(String playerName, String message) {
		Optional<ProxiedPlayer> optionalPlayer = BungeeUtil.getProxiedPlayer(playerName);

		if (!optionalPlayer.isPresent())
			return;

		optionalPlayer.get().disconnect(new TextComponent(message));
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
		return BungeeApplication.getPlugin().getProxy().getPluginManager().getPlugin("LuckPerms") != null;
	}
}
