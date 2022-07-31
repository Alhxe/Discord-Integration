package di.dilogin;

import java.util.logging.Level;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import di.dicore.DIApi;
import di.dilogin.controller.DBController;
import di.dilogin.controller.DILoginController;
import di.dilogin.discord.command.DiscordRegisterCommand;
import di.dilogin.discord.event.GuildMemberRoleEvent;
import di.dilogin.discord.event.UserReactionMessageEvent;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.command.ForceLoginCommand;
import di.dilogin.minecraft.command.RegisterCommand;
import di.dilogin.minecraft.command.UnregisterCommand;
import di.dilogin.minecraft.event.UserBlockEvents;
import di.dilogin.minecraft.event.UserLeaveEvent;
import di.dilogin.minecraft.event.UserLoginEventImpl;
import di.dilogin.minecraft.event.UserPreLoginEvent;
import di.dilogin.minecraft.event.UserTeleportEvents;
import di.dilogin.minecraft.ext.authme.AuthmeEvents;
import di.dilogin.minecraft.ext.authme.UserLoginEventAuthmeImpl;
import di.dilogin.minecraft.ext.luckperms.LuckPermsEvents;
import di.dilogin.minecraft.ext.nlogin.UnregisterNLoginEvents;
import di.dilogin.minecraft.ext.nlogin.UserLoginEventNLoginImpl;
import di.internal.exception.NoApiException;

/**
 * Main Discord Integration Login class.
 */
public class BukkitApplication extends JavaPlugin {

	/**
	 * Discord Integration Core Api.
	 */
	private static DIApi api;

	/**
	 * Main DILogin plugin.
	 */
	private static Plugin plugin;

	@Override
	public void onEnable() {
		getLogger().info("Plugin started");
		plugin = getPlugin(getClass());

		connectWithCoreApi();
		initCommands();
		initEvents();
		initDiscordEvents();
		initDiscordCommands();
		DBController.getConnect();
	}

	@Override
	public void onDisable() {
		TmpCache.clearAll();
	}

	/**
	 * @return Discord Integration Api.
	 */
	public static DIApi getDIApi() {
		return api;
	}

	/**
	 * @return Get Main Bukkit plugin.
	 */
	public static Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Add the commands to bukkit.
	 */
	private void initCommands() {
		initUniqueCommand("diregister", (CommandExecutor) new RegisterCommand());
		initUniqueCommand("forcelogin", (CommandExecutor) new ForceLoginCommand());
		initUniqueCommand("unregister", (CommandExecutor) new UnregisterCommand());
	}

	/**
	 * Add to each command that the server must respond in case it does not have
	 * permissions.
	 * 
	 * @param command  Bukkit command.
	 * @param executor CommandExecutor.
	 */
	private void initUniqueCommand(String command, CommandExecutor executor) {
		getCommand(command).setExecutor(executor);
		getCommand(command).setPermissionMessage(api.getCoreController().getLangManager().getString("no_permission"));
	}

	/**
	 * Connect with DIApi.
	 */
	private void connectWithCoreApi() {
		if (plugin.getServer().getPluginManager().getPlugin("DICore").isEnabled()) {
			try {
				api = new DIApi(plugin, this.getClassLoader(), true, true);
			} catch (NoApiException e) {
				e.printStackTrace();
			}
		} else {
			plugin.getLogger().log(Level.SEVERE,
					"Failed to connect to DICore plugin. Check if it has been turned on correctly.");
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}

	/**
	 * Init Bukkit events.
	 */
	private void initEvents() {
		if (DILoginController.isLuckPermsEnabled()) {
			getPlugin().getLogger().info("LuckPerms detected, starting plugin compatibility.");
			new LuckPermsEvents();
		}
		if (DILoginController.isAuthmeEnabled()) {
			getPlugin().getLogger().info("Authme detected, starting plugin compatibility.");
			getServer().getPluginManager().registerEvents(new UserLoginEventAuthmeImpl(), plugin);
			getServer().getPluginManager().registerEvents(new AuthmeEvents(), plugin);
		} else if (DILoginController.isNLoginEnabled()) { //
			getPlugin().getLogger().info("nLogin detected, starting plugin compatibility.");
			try {
				Class.forName("com.nickuc.login.api.nLoginAPIHolder");
				getServer().getPluginManager().registerEvents(new UnregisterNLoginEvents(), plugin);
				getServer().getPluginManager().registerEvents(new UserLoginEventNLoginImpl(), plugin);
			} catch (ClassNotFoundException e) {
				getLogger().severe("You are using the old version of nLogin.");
				getLogger().severe("Please upgrade to version 10.");
			}
		} else {
			getServer().getPluginManager().registerEvents(new UserLoginEventImpl(), plugin);
			getServer().getPluginManager().registerEvents(new UserBlockEvents(), plugin);
		}
		getServer().getPluginManager().registerEvents(new UserLeaveEvent(), plugin);
		getServer().getPluginManager().registerEvents(new UserTeleportEvents(), plugin);
		getServer().getPluginManager().registerEvents(new UserPreLoginEvent(), plugin);
	}

	/**
	 * Records Discord events.
	 */
	private void initDiscordEvents() {
		api.registerDiscordEvent(new UserReactionMessageEvent());
		if (DILoginController.isSyncroRolEnabled()) {
			api.registerDiscordEvent(new GuildMemberRoleEvent());
		}
	}

	/**
	 * Init discord commands.
	 */
	private void initDiscordCommands() {
		api.registerDiscordCommand(new DiscordRegisterCommand());
	}

}
