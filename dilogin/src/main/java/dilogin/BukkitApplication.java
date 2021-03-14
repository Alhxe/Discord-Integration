package dilogin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import dicore.DIApi;
import utils.exception.NoApiException;

/**
 * Main Discord Integration Login class.
 */
public class BukkitApplication extends JavaPlugin {

	/**
	 * Discord Integration Core Api.
	 */
	private DIApi diapi;

	private Plugin plugin;
	
	@Override
	public void onEnable() {
		getLogger().info("Plugin started");
		plugin = getPlugin(getClass());
		
		connectWithCoreApi();
		
		diapi.getInternalController().getBot().getCommandHandler().registerCommand(new PingCommand());

	}

	private void connectWithCoreApi() {
		try {
			diapi = new DIApi(plugin);
		} catch (NoApiException e) {
			e.printStackTrace();
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}
}
