package di.dilogin.minecraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqliteImpl;

/**
 * Command to unregister the account.
 */
public class UnregisterCommand implements CommandExecutor {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = new DIUserDaoSqliteImpl();

	/**
	 * Main plugin.
	 */
	public final Plugin plugin = BukkitApplication.getPlugin();

	/**
	 * Main api.
	 */
	public final DIApi api = BukkitApplication.getDIApi();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 0) {
			sender.sendMessage(api.getCoreController().getLangManager().getString("no_args"));
			return false;
		}

		String nick = args[0];
		Player player = plugin.getServer().getPlayer(nick);

		if (player == null) {
			sender.sendMessage(LangManager.getString("no_player").replace("%nick%", nick));
			return true;
		}
		if (!userDao.contains(player.getName())) {
			sender.sendMessage(LangManager.getString(player, "user_not_registered"));
			return true;
		}

		userDao.remove(player.getName());
		sender.sendMessage(LangManager.getString(player, "unregister_success"));
		player.kickPlayer(LangManager.getString(player, "unregister_kick"));
		return true;
	}

}
