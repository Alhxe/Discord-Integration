package dilogin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import dicore.DIApi;
import dilogin.minecraft.command.ForceLoginCommand;
import dilogin.minecraft.command.RegisterCommand;
import dilogin.minecraft.command.UnregisterCommand;
import dilogin.minecraft.event.UserLoginEvent;
import utils.exception.NoApiException;

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
	private Plugin plugin;

	@Override
	public void onEnable() {
		getLogger().info("Plugin started");
		plugin = getPlugin(getClass());

		connectWithCoreApi();
		initCommands();
		initEvents();
	}

	/**
	 * @return Discord Integration Api.
	 */
	public static DIApi getDIApi() {
		return api;
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
	private void connectWithCoreApi() {
		try {
			api = new DIApi(plugin);
		} catch (NoApiException e) {
			e.printStackTrace();
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}
	
	private void initEvents() {
		getServer().getPluginManager().registerEvents(new UserLoginEvent(), plugin);
	}
}
