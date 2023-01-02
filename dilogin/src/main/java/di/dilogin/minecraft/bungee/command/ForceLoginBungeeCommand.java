package di.dilogin.minecraft.bungee.command;

import java.time.Duration;
import java.util.Optional;

import di.dilogin.BungeeApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Command to force a user login.
 */
public class ForceLoginBungeeCommand extends Command {

	public ForceLoginBungeeCommand() {
		super("Forcelogin", "sdl.forcelogin");
	}

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main plugin.
	 */
	public final Plugin plugin = BungeeApplication.getPlugin();
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		String nick = args[0];
		ProxiedPlayer player = BungeeApplication.getPlugin().getProxy().getPlayer(nick);

		if (player == null) {
			sender.sendMessage(LangManager.getString("no_player").replace("%nick%", nick));
			return;
		}
		if (!userDao.contains(player.getName())) {
			sender.sendMessage(LangManager.getString(player.getName(), "user_not_registered"));
			return;
		}
		if (!TmpCache.containsLogin(player.getName())) {
			sender.sendMessage(LangManager.getString(player.getName(), "forcelogin_user_connected"));
			return;
		}
		editMessage(player);
		MainController.getDILoginController().loginUser(player.getName(), null);
		sender.sendMessage(LangManager.getString(player.getName(), "forcelogin_success"));
		return;
	}

	/**
	 * Edit the message of the user.
	 * @param player The minecraft player.
	 */
	public static void editMessage(ProxiedPlayer player) {
		Optional<TmpMessage> tmpMessageOpt = TmpCache.getLoginMessage(player.getName());
		if (!tmpMessageOpt.isPresent())
			return;
		
		User user = tmpMessageOpt.get().getUser();
		Message message = tmpMessageOpt.get().getMessage();
		
		MessageEmbed embed = MainController.getDILoginController().getEmbedBase()
				.setTitle(LangManager.getString(user, player.getName(), "login_discord_title"))
				.setDescription(LangManager.getString(user, player.getName(), "login_discord_forced")).build();
		
		message.editMessageEmbeds(embed).delay(Duration.ofSeconds(60)).flatMap(Message::delete).queue();
	}
}
