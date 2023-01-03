package di.dilogin.minecraft.bungee.command;

import java.util.Optional;

import di.dicore.api.DIApi;
import di.dilogin.BungeeApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bungee.BungeeUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Command to unregister the account.
 */
public class UnregisterBungeeCommand extends Command {

	public UnregisterBungeeCommand() {
		super("Unregister", "sdl.unregister");
	}

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main plugin.
	 */
	public final Plugin plugin = BungeeApplication.getPlugin();

	/**
	 * Main api.
	 */
	public final DIApi api = MainController.getDIApi();

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(api.getCoreController().getLangManager().getString("no_args"));
			return;
		}

		String nick = args[0];
		Optional<DIUser> optUser = userDao.get(nick);

		if (!optUser.isPresent()) {
			sender.sendMessage(LangManager.getString("no_player").replace("%nick%", nick));
			return;
		}
		DIUser user = optUser.get();
		Optional<ProxiedPlayer> optPlayer = BungeeUtil.getProxiedPlayer(nick);
		
		if(!optPlayer.isPresent())
			return;
		
		ProxiedPlayer player = optPlayer.get();
		sender.sendMessage(LangManager.getString(nick, "unregister_success"));
		
		if(player!=null)
			MainController.getDILoginController().kickPlayer(nick, LangManager.getString(player.getName(), "unregister_kick"));
		userDao.remove(user);
		return;
	}

}
