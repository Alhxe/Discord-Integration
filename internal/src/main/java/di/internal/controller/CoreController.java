package di.internal.controller;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.entity.DiscordBot;
import di.internal.exception.NoApiException;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Internal Controller of Plugin.
 */
@Getter
public class CoreController implements PluginController {

	/**
	 * The driver for the plugin configuration file.
	 */
	private ConfigManager configManager;

	/**
	 * The driver for the plugin lang file.
	 */
	private YamlManager langManager;

	/**
	 * The bukkit plugin.
	 */
	private Plugin plugin;

	/**
	 * Contains the bot config information.
	 */
	private DiscordBot bot;

	/**
	 * Path of the main plugin folder.
	 */
	private File dataFolder;

	/**
	 * Main constructor;
	 * 
	 * @param plugin      Bukkit plugin.
	 * @param classLoader Class loader.
	 * @throws NoApiException
	 */
	public CoreController(Plugin plugin, ClassLoader classLoader) {
		this.plugin = plugin;
		this.dataFolder = plugin.getDataFolder();
		this.configManager = new ConfigManager(this, plugin.getDataFolder());
		this.langManager = new YamlManager(this, "lang.yml", plugin.getDataFolder(), classLoader);
		this.bot = initBot();
	}

	/**
	 * Save resource in plugin folder.
	 * 
	 * @param name Name of file.
	 */
	public void saveResource(String name) {
		plugin.saveResource(name, false);
	}

	/**
	 * Init Discord Bot.
	 * 
	 * @return Finished bot object.
	 */
	private DiscordBot initBot() {
		long serverid = 0;

		String token = configManager.getString("bot_token");
		try {
			serverid = configManager.getLong("discord_server_id");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE,
					"Failed to get server ID. Modify the config.yml file to be able to start the plugin.");
		}
		String prefix = configManager.getString("discord_server_prefix");

		if (token == null || prefix == null || serverid == 0L) {
			this.plugin.getLogger().log(Level.SEVERE,
					"Failed to load the data required to start the bot. Did you enter the server ID, token and prefix correctly?");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return null;
		}

		this.plugin.getLogger().info("Starting Bot");
		return new DiscordBot(prefix, serverid, token, plugin);
	}

	/**
	 * @return Discord Api.
	 */
	public JDA getDiscordApi() {
		return this.bot.getApi();
	}
	
	/**
	 * 
	 * @return Main server guild.
	 */
	public Guild getGuild() {
		return bot.getApi().getGuildById(bot.getServerid());
	}

}