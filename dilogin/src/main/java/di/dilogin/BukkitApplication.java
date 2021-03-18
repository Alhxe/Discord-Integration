package di.dilogin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import di.dicore.DIApi;
import di.dilogin.controller.DBController;
import di.dilogin.discord.command.DiscordRegisterCommand;
import di.dilogin.discord.event.UserReactionMessageEvent;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.command.ForceLoginCommand;
import di.dilogin.minecraft.command.RegisterCommand;
import di.dilogin.minecraft.command.UnregisterCommand;
import di.dilogin.minecraft.event.UserBlockEvents;
import di.dilogin.minecraft.event.UserLeaveEvent;
import di.dilogin.minecraft.event.UserLoginEvent;
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
		initUniqueCommand("register", (CommandExecutor) new RegisterCommand());
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
		getCommand(command)
				.setPermissionMessage(api.getCoreController().getLangManager().getString("no_permission"));
	}

	/**
	 * Connect with DIApi.
	 */
	private static void connectWithCoreApi() {
		try {
			api = new DIApi(plugin);
		} catch (NoApiException e) {
			e.printStackTrace();
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}
	
	/**
	 * Init Bukkit events.
	 */
	private void initEvents() {
		getServer().getPluginManager().registerEvents(new UserLoginEvent(), plugin);
		getServer().getPluginManager().registerEvents(new UserLeaveEvent(), plugin);
		getServer().getPluginManager().registerEvents(new UserBlockEvents(), plugin);
	}
	
	/**
	 * Records Discord events.
	 */
	private void initDiscordEvents() {
		api.registerDiscordEvent(new UserReactionMessageEvent());
	}
	
	private void initDiscordCommands() {
		api.registerDiscordCommand(new DiscordRegisterCommand());
	}
	
}
