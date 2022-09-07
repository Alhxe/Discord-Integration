package di.internal.controller.impl;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import di.internal.controller.ChannelController;
import di.internal.interceptor.ChannelBukkitInterceptor;
import di.internal.utils.Util;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Class that manages the channel events.
 */
public class ChannelControllerBukkitImpl implements ChannelController {

    /**
     * Bukkit plugin.
     */
    private final Plugin plugin;

    /**
     * Bukkit channel message interceptor.
     */
    private final ChannelBukkitInterceptor channelBukkitInterceptor;

    /**
     * Main Class Constructor.
     *
     * @param plugin Bukkit plugin.
     */
    public ChannelControllerBukkitImpl(Plugin plugin) {
        this.plugin = plugin;
        this.channelBukkitInterceptor = new ChannelBukkitInterceptor();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", channelBukkitInterceptor);
    }

    @Override
    public void sendMessageToPlugin(String playerName, String message) {
        String subChannel = Util.getRandomSubChannel(playerName);
        sendMessage(subChannel, playerName, message);
    }

    @Override
    public void sendMessageToPlugin(String subChannel, String playerName, String message) {
        sendMessage(subChannel, playerName, message);
    }

    @Override
    public CompletableFuture<String> sendMessageAndWaitResponse(String playerName, String message) {
        String subChannel = Util.getRandomSubChannel(playerName);
        return sendMessageWithResponse(subChannel, playerName, message);
    }

    @Override
    public CompletableFuture<String> sendMessageAndWaitResponse(String subChannel, String playerName, String message) {
        return sendMessageWithResponse(subChannel, playerName, message);
    }

    /**
     * Sends a message to the plugin.
     *
     * @param subChannel Bungee SubChannel.
     * @param playerName Player name.
     * @param message    Message to send.
     */
    public void sendMessage(String subChannel, String playerName, String message) {
        Optional<Player> playerOptional = Optional.ofNullable(plugin.getServer().getPlayer(playerName));

        if (playerOptional.isPresent()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(subChannel);
            out.writeUTF(message);

            playerOptional.get().getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        }
    }

    /**
     * Sends a message to the plugin and waits for a response.
     *
     * @param subChannel Bungee SubChannel.
     * @param playerName Player name.
     * @param message    Message to send.
     * @return CompletableFuture with the response.
     */
    public CompletableFuture<String> sendMessageWithResponse(String subChannel, String playerName, String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Optional<Player> playerOptional = Optional.ofNullable(plugin.getServer().getPlayer(playerName));

        if (!playerOptional.isPresent())
            future.complete("error");

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(message);

        Player player = playerOptional.get();
        player.getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        channelBukkitInterceptor.addMessage(subChannel);

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            boolean isFinished = false;
            for (int i = 0; !isFinished && i <= 10; i++) {
                if (channelBukkitInterceptor.containsMessage(subChannel + "answer")) {
                    future.complete(channelBukkitInterceptor.getMessage(subChannel + "answer"));
                    isFinished = true;
                }
            }
            if (!isFinished) {
                future.complete("error");
            }
            channelBukkitInterceptor.removeMessage(subChannel);
        }, 20);
        return future;
    }
}
