package di.internal.controller;

import org.bukkit.plugin.Plugin;

import di.internal.controller.file.ConfigManager;

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
