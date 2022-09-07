package di.dicore.bungee.controller;

import di.dicore.BungeeApplication;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.Future;

public class BotController {


    public static boolean sendMessage(ProxiedPlayer player, String message) {
        try {
            BungeeApplication.getInternalController().getLogger().info("Ejecutando futuro...");

            Future<String> response = BungeeApplication.getChannelController()
                    .sendMessageAndWaitResponse(player.getName(), message);

            if (response.get().equals("error")) {
                BungeeApplication.getInternalController().getLogger().info("Error al enviar el mensaje");
                return false;
            } else {
                BungeeApplication.getInternalController().getLogger().info("Mensaje recibido" + response.get());
                return true;
            }
        } catch (Exception e) {
            BungeeApplication.getInternalController().getLogger().info("Error al enviar el mensaje");
            return false;
        }
    }

}
