package di.dilogin.minecraft.bukkit.command;

import di.dilogin.BukkitApplication;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class EjemploCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            CompletableFuture<String> futuro = BukkitApplication.getDIApi().getInternalController()
                    .getChannelController().sendMessageAndWaitResponse(player.getName(), "Hola");

            futuro.whenCompleteAsync((s, throwable) -> {
               sender.getServer().getLogger().info("Respuesta: " + s);
            });


            return true;
        }
        return false;
    }
}
