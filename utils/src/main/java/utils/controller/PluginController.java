package utils.controller;

import org.bukkit.plugin.Plugin;

import utils.controller.file.ConfigManager;

/**
 * Interace of internal controllers.
 */
public interface PluginController {
	
	/**
	 * @return Plugin configuration controller.
	 */
	ConfigManager getConfigManager();
	
	/**
	 * @return Bukkit Plugin.
	 */
	Plugin getPlugin();

}
