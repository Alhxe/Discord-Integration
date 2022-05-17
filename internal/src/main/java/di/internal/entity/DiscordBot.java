package di.internal.entity;

import java.util.Optional;

import javax.security.auth.login.LoginException;

import org.bukkit.plugin.Plugin;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * Contains the main information of the bot.
 **/
@Getter
@Setter
public class DiscordBot {

	/**
	 * Javacord Api.
	 */
	private JDA api;

	/**
	 * Bot prefix.
	 */
	private String prefix;

	/**
	 * Main server id.
	 */
	private long serverid;

	/**
	 * Bot CommandHandler.
	 */
	private CommandHandler commandHandler;

	/**
	 * Main Controller.
	 * 
	 * @param prefix   Bot prefix.
	 * @param serverid Main server id.
	 * @param token    Bot token.
	 * @param plugin   Bukkit plugin.
	 */
	public DiscordBot(String prefix, long serverid, String token, Plugin plugin) {
		this.prefix = prefix;
		this.serverid = serverid;
		initBot(token, plugin);
	}

	/**
	 * Init bot.
	 * 
	 * @param token  Bot token.
	 * @param plugin Bukkit plugin.
	 */
	public void initBot(String token, Plugin plugin) {
		try {
			this.api = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_PRESENCES,
					GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS).build();
			api.awaitReady();
			onConnectToDiscord(plugin);
			checkPermissions(plugin);
		} catch (LoginException e) {
			plugin.getLogger().warning("The Bot failed to start. You have not entered a valid token.");
			plugin.getPluginLoader().disablePlugin(plugin);
		} catch (InterruptedException e) {
			plugin.getLogger().warning("The Bot failed to start. Reason:");
			e.printStackTrace();
			plugin.getPluginLoader().disablePlugin(plugin);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * When the bot starts successfully it passes here.
	 * 
	 * @param plugin Bukkit plugin.
	 */
	private void onConnectToDiscord(Plugin plugin) {
		plugin.getLogger().info("Bot started");
		this.commandHandler = new CommandHandler(prefix);
		api.addEventListener(this.commandHandler);
	}

	/**
	 * Check discord server permissions.
	 * 
	 * @param api JDA Api.
	 */
	private void checkPermissions(Plugin plugin) {
		Optional<Guild> guildOpt = Optional.ofNullable(api.getGuildById(serverid));

		if (!guildOpt.isPresent()) {
			plugin.getLogger().warning("I could not find the server with ID " + serverid);
			return;
		}

		Guild guild = guildOpt.get();
		Member bot = guild.getSelfMember();

		if (bot.hasPermission(Permission.ADMINISTRATOR))
			return;

		if (!bot.hasPermission(Permission.MESSAGE_WRITE))
			plugin.getLogger()
					.warning("The bot does not have writes permission on the server, this could cause a conflict!");

		if (!bot.hasPermission(Permission.MESSAGE_MANAGE))
			plugin.getLogger()
					.warning("The bot is not allowed to handle messages on the server, this could lead to conflict!");

		if (!bot.hasPermission(Permission.MESSAGE_ADD_REACTION))
			plugin.getLogger().warning(
					"The bot does not have permission to add reactions on the server, this could cause conflict!");
	}
}