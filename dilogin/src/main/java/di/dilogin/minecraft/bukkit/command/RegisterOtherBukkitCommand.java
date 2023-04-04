package di.dilogin.minecraft.bukkit.command;

import di.dicore.api.DIApi;
import di.dilogin.controller.MainController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.utils.Util;
import net.dv8tion.jda.api.entities.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Command to register as a user.
 */
public class RegisterOtherBukkitCommand implements CommandExecutor {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main api.
	 */
	private final DIApi api = MainController.getDIApi();

	/**
	 * Main command body.
	 * 
	 * @param sender  The sender of the command.
	 * @param command The command.
	 * @param label   The label of the command.
	 * @param args    The arguments of the command.
	 * @return True if the command was executed.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
			return false;
		}

		if (args.length != 2) {
			sender.sendMessage(LangController.getString("register_other_arguments"));
			return true;
		}

		String playerToRegister = args[0];
		if (userDao.contains(playerToRegister)) {
			sender.sendMessage(LangController.getString(playerToRegister, "register_other_already_exists"));
			return true;
		}

		Optional<User> userOpt = registerById(args[1]);
		if (!userOpt.isPresent()) {
			return true;
		}

		User user = userOpt.get();

		userDao.add(new DIUser(playerToRegister, Optional.of(user)));

		sender.sendMessage(LangController.getString(playerToRegister, "register_other_success"));
		Player playerToRegisterOnServer = sender.getServer().getPlayer(playerToRegister);

		if (playerToRegisterOnServer != null) {
			playerToRegisterOnServer.sendMessage(LangController.getString(playerToRegisterOnServer.getName(), "register_other_success"));
			TmpCache.removeRegister(playerToRegisterOnServer.getName());
		}

		return true;

	}


	/**
	 * Get the user if his registration method is by discord id.
	 * 
	 * @param string Args from the command.
	 * @return Posible user.
	 */
	private Optional<User> registerById(String string) {
		if (!idIsValid(string))
			return Optional.empty();

		return Util.getDiscordUserById(api.getCoreController().getDiscordApi().get(), Long.parseLong(string));
	}

	/**
	 * Check if the user entered exists.
	 * 
	 * @param id Discord ID.
	 * @return True if id is valid.
	 */
	private static boolean idIsValid(String id) {
		try {
			Long.parseLong(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}