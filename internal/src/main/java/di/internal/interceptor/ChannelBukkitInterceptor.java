package di.internal.interceptor;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Channel events.
 */
public class ChannelBukkitInterceptor implements PluginMessageListener {

    /**
     * Contain the messages that have been received the answers.
     */
    private final HashMap<String, String> messagesAnswer = new HashMap<>();

    /**
     * Contain the subChannels that have been send to get answer.
     */
    private final List<String> messages = new ArrayList<>();

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        String data1 = in.readUTF();

        if (subChannel.contains("answer") && messages.contains(subChannel.replaceAll("answer", "")))
            messagesAnswer.put(subChannel, data1);
    }

    /**
     * Check if the message has been received.
     *
     * @param subChannel SubChannel name.
     * @return true if message is present.
     */
    public boolean containsMessage(String subChannel) {

        return messagesAnswer.containsKey(subChannel);
    }

    /**
     * Get the message.
     *
     * @param subChannel SubChannel name.
     * @return Message.
     */
    public String getMessage(String subChannel) {
        return messagesAnswer.get(subChannel);
    }

    /**
     * Remove the subChannel from the list.
     *
     * @param subChannel SubChannel name.
     */
    public void removeMessage(String subChannel) {
        messagesAnswer.remove(subChannel + "answer");
        messages.remove(subChannel);
    }

    /**
     * Add the subChannel to the list.
     * @param subChannel SubChannel name.
     */
    public void addMessage(String subChannel) {
        messages.add(subChannel);
    }
}
