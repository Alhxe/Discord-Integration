package di.dilogin.minecraft.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginMessageEvent implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        System.out.println("Message received" + channel + " " + message.toString());
    }
}
