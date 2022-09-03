package di.internal.entity;

import java.util.Optional;

import javax.security.auth.login.LoginException;

import di.internal.controller.CoreController;
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
	 * CoreController of plugin.
	 */
	private final CoreController controller;

	/**
	 * Main Controller.
	 * 
	 * @param prefix   Bot prefix.
	 * @param serverid Main server id.
	 * @param token    Bot token.
	 * @param controller  CoreController of plugin.
	 */
	public DiscordBot(String prefix, long serverid, String token, CoreController controller) {
		this.prefix = prefix;
		this.serverid = serverid;
		this.controller = controller;
		initBot(token);
	}

	/**
	 * Init bot.
	 * 
	 * @param token  Bot token.
	 */
	public void initBot(String token) {
		try {
			this.api = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_PRESENCES,
					GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS).build();
			api.awaitReady();
			onConnectToDiscord();
			checkPermissions();
		} catch (LoginException e) {
			controller.getLogger().warning("The Bot failed to start. You have not entered a valid token.");
			controller.disablePlugin();
		} catch (InterruptedException e) {
			controller.getLogger().warning("The Bot failed to start. Reason:");
			e.printStackTrace();
			controller.disablePlugin();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * When the bot starts successfully it passes here.
	 */
	private void onConnectToDiscord() {
		controller.getLogger().info("Bot started");
		this.commandHandler = new CommandHandler(prefix);
		api.addEventListener(this.commandHandler);
	}

	/**
	 * Check discord server permissions.
	 */
	private void checkPermissions() {
		Optional<Guild> guildOpt = Optional.ofNullable(api.getGuildById(serverid));

		if (!guildOpt.isPresent()) {
			controller.getLogger().warning("I could not find the server with ID " + serverid);
			return;
		}

		Guild guild = guildOpt.get();
		Member bot = guild.getSelfMember();

		if (bot.hasPermission(Permission.ADMINISTRATOR))
			return;

		if (!bot.hasPermission(Permission.MESSAGE_WRITE))
			controller.getLogger()
					.warning("The bot does not have writes permission on the server, this could cause a conflict!");

		if (!bot.hasPermission(Permission.MESSAGE_MANAGE))
			controller.getLogger()
					.warning("The bot is not allowed to handle messages on the server, this could lead to conflict!");

		if (!bot.hasPermission(Permission.MESSAGE_ADD_REACTION))
			controller.getLogger().warning(
					"The bot does not have permission to add reactions on the server, this could cause conflict!");
	}
}