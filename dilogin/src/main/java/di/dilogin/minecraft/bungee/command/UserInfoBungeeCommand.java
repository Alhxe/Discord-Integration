package di.dilogin.minecraft.bungee.command;

import java.util.Optional;

import di.dilogin.controller.MainController;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to display user information.
 */
public class UserInfoBungeeCommand extends Command  {

    public UserInfoBungeeCommand() {
    	super(CommandAliasController.getAlias("userinfo_command"), "sdl.userinfo");
	}

	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
        // Check if the player has provided a username
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify the username of the player.");
        }

        String playerName = args[0];

        // Retrieve user information from the database
        Optional<DIUser> userOpt = userDao.get(playerName);

        if (userOpt.isPresent()) {
            DIUser user = userOpt.get();
            sender.sendMessage(ChatColor.GREEN + "Player Name: " + user.getPlayerName());
            sender.sendMessage(ChatColor.GREEN + "Discord User: " + (user.getPlayerDiscord().isPresent() ? user.getPlayerDiscord().get().getName() : "Not linked to Discord"));
            sender.sendMessage(ChatColor.GREEN + "Discord ID: " + (user.getPlayerDiscord().isPresent() ? user.getPlayerDiscord().get().getId() : "Not linked to Discord"));
        } else {
            sender.sendMessage(ChatColor.RED + "Player not found in the database.");
        }
	}
}
