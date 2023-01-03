package di.dilogin.minecraft.bukkit.command;

import java.util.Optional;

import di.dilogin.controller.MainController;
import di.dilogin.controller.file.LangController;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;

/**
 * Command to unregister the account.
 */
public class UnregisterBukkitCommand implements CommandExecutor {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main plugin.
	 */
	public final Plugin plugin = BukkitApplication.getPlugin();

	/**
	 * Main api.
	 */
	public final DIApi api = MainController.getDIApi();

	/**
	 * Main command body.
	 * @param sender The sender of the command.
	 * @param command The command.
	 * @param label The label of the command.
	 * @param args The arguments of the command.
	 * @return True if the command was executed.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 0) {
			sender.sendMessage(api.getCoreController().getLangManager().getString("no_args"));
			return true;
		}

		String nick = args[0];
		Optional<DIUser> optUser = userDao.get(nick);

		if (!optUser.isPresent()) {
			sender.sendMessage(LangController.getString("no_player").replace("%nick%", nick));
			return true;
		}
		DIUser user = optUser.get();
		Player player = sender.getServer().getPlayer(nick);
		sender.sendMessage(LangController.getString(nick, "unregister_success"));
		
		if(player!=null)
			player.kickPlayer(LangController.getString(player.getName(), "unregister_kick"));
		
		userDao.remove(user);
		return true;
	}

}
