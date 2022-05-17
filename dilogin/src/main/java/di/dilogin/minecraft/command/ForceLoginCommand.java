package di.dilogin.minecraft.command;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

/**
 * Command to force a user login.
 */
public class ForceLoginCommand implements CommandExecutor {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = DILoginController.getDIUserDao();

	/**
	 * Main plugin.
	 */
	public final Plugin plugin = BukkitApplication.getPlugin();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String nick = args[0];
		Player player = plugin.getServer().getPlayer(nick);

		if (player == null) {
			sender.sendMessage(LangManager.getString("no_player").replace("%nick%", nick));
			return false;
		}
		if (!userDao.contains(player.getName())) {
			sender.sendMessage(LangManager.getString(player, "user_not_registered"));
			return false;
		}
		if (!TmpCache.containsLogin(player.getName())) {
			sender.sendMessage(LangManager.getString(player, "forcelogin_user_connected"));
			return false;
		}
		editMessage(player);
		DILoginController.loginUser(player, null);
		sender.sendMessage(LangManager.getString(player, "forcelogin_success"));
		return true;

	}

	public static void editMessage(Player player) {
		Optional<TmpMessage> tmpMessageOpt = TmpCache.getLoginMessage(player.getName());
		if (!tmpMessageOpt.isPresent())
			return;
		
		User user = tmpMessageOpt.get().getUser();
		Message message = tmpMessageOpt.get().getMessage();
		
		MessageEmbed embed = DILoginController.getEmbedBase()
				.setTitle(LangManager.getString(user, player, "login_discord_title"))
				.setDescription(LangManager.getString(user, player, "login_discord_forced")).build();
		
		message.editMessage(embed).delay(Duration.ofSeconds(60)).flatMap(Message::delete).queue();
	}

}
