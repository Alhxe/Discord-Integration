package di.dilogin.discord.command;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqliteImpl;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.entity.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Command to register as a user.
 */
public class RegisterCommand implements DiscordCommand {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = new DIUserDaoSqliteImpl();

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	@Override
	public void execute(String message, MessageReceivedEvent event) {
		// Check if user exists.
		if (userDao.containsDiscordId(event.getAuthor().getIdLong())) {
			event.getChannel().sendMessage(LangManager.getString("register_already_exists"))
					.delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
			return;
		}
		
		// Check account limits.
		if (userDao.getDiscordUserAccounts(event.getAuthor()) >= api.getInternalController().getConfigManager()
				.getInt("register_max_discord_accounts")) {
			event.getChannel().sendMessage(LangManager.getString("register_max_accounts"))
					.delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
			return;
		}

		// Check arguments.
		if (message.equals("") || message.isEmpty() || message.isBlank()) {
			event.getChannel().sendMessage(LangManager.getString("register_arguments")).delay(Duration.ofSeconds(10))
					.flatMap(Message::delete).queue();
			return;
		}

		// Check code.
		Optional<TmpMessage> tmpMessageOpt = TmpCache.getRegisterMessageByCode(message);
		if (!tmpMessageOpt.isPresent()) {
			event.getChannel().sendMessage(LangManager.getString("register_code_not_found"))
					.delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
			return;
		}

		Player player = tmpMessageOpt.get().getPlayer();
		player.sendMessage(LangManager.getString(event.getAuthor(), player, "register_success"));
		MessageEmbed messageEmbed = getEmbedMessage(player, event.getAuthor());
		event.getChannel().sendMessage(messageEmbed).delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
		TmpCache.removeRegister(player.getName());
		userDao.add(new DIUser(player, event.getAuthor()));
		DILoginController.loginUser(player);
	}

	@Override
	public String getAlias() {
		return "register";
	}
	
	/**
	 * Create the log message according to the configuration.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(Player player, User user) {
		EmbedBuilder embedBuilder = DILoginController.getEmbedBase().setTitle(LangManager.getString(player, "register_discord_title"))
				.setDescription(LangManager.getString(user, player, "register_discord_success"));
		return embedBuilder.build();
	}

}
