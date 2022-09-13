package di.dilogin.minecraft.bukkit.command;

import di.dilogin.BukkitApplication;
import di.internal.dto.DIUserDto;
import di.internal.dto.FileDto;
import di.internal.dto.converter.JsonConverter;
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
                    .getChannelController().sendMessageAndWaitResponse(player.getName(), "getDIUser", player.getName());

            futuro.whenCompleteAsync((s, throwable) -> {
                JsonConverter<DIUserDto> converter = new JsonConverter(DIUserDto.class);
               sender.getServer().getLogger().info("Respuesta: " + converter.getDto(s));
            });

            CompletableFuture<String> futuro2 = BukkitApplication.getDIApi().getInternalController()
                    .getChannelController().sendMessageAndWaitResponse(player.getName(), "getConfigFile", player.getName());

            futuro2.whenCompleteAsync((s, throwable) -> {
                JsonConverter<FileDto> converter = new JsonConverter(FileDto.class);
                sender.getServer().getLogger().info("Respuesta: " + converter.getDto(s));
            });

            return true;
        }
        return false;
    }
}
