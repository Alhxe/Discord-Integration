package di.dilogin.minecraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String nick = args[0];
		Player player = plugin.getServer().getPlayer(nick);

		if (player == null) {
			plugin.getLogger().info(LangManager.getString("no_player").replace("%nick%", nick));
			return true;
		}
		if (!userDao.contains(player.getName())) {
			plugin.getLogger().info(LangManager.getString(player, "user_not_registered"));
			return true;
		}

		userDao.remove(player.getName());
		plugin.getLogger().info(LangManager.getString(player, "unregister_success"));
		player.kickPlayer(LangManager.getString(player, "unregister_kick"));
		return false;
	}

}
