package di.dilogin.minecraft.bukkit.command;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import net.md_5.bungee.api.ChatColor;

/**
 * Command to display user information.
 */
public class UserInfoBukkitCommand implements CommandExecutor {

    private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

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
        // Check if the player has provided a username
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify the username of the player.");
            return false;
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

        return true;
    }
}
