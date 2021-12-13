package di.dilogin.minecraft.command;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.DIUser;

/**
 * Command to unregister the account.
 */
public class UnregisterCommand implements CommandExecutor {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = new DIUserDaoSqlImpl();

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
		Optional<DIUser> optUser = userDao.get(nick);

		if (!optUser.isPresent()) {
			sender.sendMessage(LangManager.getString("no_player").replace("%nick%", nick));
			return true;
		}
		DIUser user = optUser.get();
		Player player = sender.getServer().getPlayer(nick);
		sender.sendMessage(LangManager.getString(nick, "unregister_success"));
		
		if(player!=null)
		player.kickPlayer(LangManager.getString(player, "unregister_kick"));
		
		userDao.remove(user);
		return true;
	}

}
