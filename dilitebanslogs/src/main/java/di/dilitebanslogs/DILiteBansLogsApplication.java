package di.dilitebanslogs;

import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import di.dicore.DIApi;
import di.internal.exception.NoApiException;
import net.dv8tion.jda.api.entities.TextChannel;

public class DILiteBansLogsApplication extends JavaPlugin {

	/**
	 * Discord Integration Core Api.
	 */
	private static DIApi api;

	/**
	 * Main DILogin plugin.
	 */
	private static Plugin plugin;

	/**
	 * Main channel to log.
	 */
	private static TextChannel channel;

	@Override
	public void onEnable() {
		getLogger().info("Starting plugin");
		plugin = (Plugin) getPlugin(getClass());
		connectWithCoreApi();
		connectWithChannel();
		initEvents();
	}

	/**
	 * @return Discord Integration Api.
	 */
	public static DIApi getDIApi() {
		return api;
	}

	/**
	 * @return Main Bukkit plugin.
	 */
	public static Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * @return Main Channel.
	 */
	public static TextChannel getChannel() {
		return channel;
	}

	/**
	 * Connect with DIApi.
	 */
	private void connectWithCoreApi() {
		try {
			api = new DIApi(plugin, this.getClassLoader(), true, true);
		} catch (NoApiException e) {
			e.printStackTrace();
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}

	/**
	 * Connect with TextChannel.
	 */
	private static void connectWithChannel() {
		final long CHANNEL_ID = api.getInternalController().getConfigManager().getLong("log_channel");

		Optional<TextChannel> textChannelOpt = Optional
				.ofNullable(api.getCoreController().getDiscordApi().getTextChannelById(CHANNEL_ID));

		if (!textChannelOpt.isPresent()) {
			getPlugin().getLogger().log(Level.SEVERE, "No channel found with " + CHANNEL_ID + " ID.");
			getPlugin().getPluginLoader().disablePlugin(plugin);
			return;
		}
		
		channel = textChannelOpt.get();
	}
	
	/**
	 * Enable events
	 */
	private static void initEvents() {
		EventsListener.init();
	}
}
