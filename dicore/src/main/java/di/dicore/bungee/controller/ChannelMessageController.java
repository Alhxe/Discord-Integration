package di.dicore.bungee.controller;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import di.dicore.BungeeApplication;
import di.internal.controller.ChannelController;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Class used to respond to plugin requests.
 */
public class ChannelMessageController implements Listener {

    @EventHandler
    public void on(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();
        String data1 = in.readUTF();

        String playerName = getPlayerNameFromSubChannel(subChannel);
        BungeeApplication.getChannelController().sendMessageToPlugin(subChannel + "answer", playerName, "respuesta concedida");
    }

    private String getPlayerNameFromSubChannel(String subChannel) {
        String playerName = "";
        for (int i = 0; i < subChannel.length(); i++) {
            char c = subChannel.charAt(i);
            if (Character.isDigit(c)) {
                break;
            }
            playerName += c;
        }
        return playerName;
    }
}
