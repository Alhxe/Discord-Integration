package dicore;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import utils.controller.InternalController;

public class BukkitApplication extends JavaPlugin {

	private static InternalController internalController;

	@Override
	public void onEnable() {
		getLogger().info("Plugin started");
		Plugin plugin = getPlugin(getClass());
		internalController = new InternalController(plugin);
	}

	@Override
	public void onDisable() {
		getLogger().info("Plugin disabled");
	}

	public static InternalController getInternalController() {
		return internalController;
	}

}
