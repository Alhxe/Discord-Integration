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
    public void sendMessageToPlugin(String playerName, String content) {
        String subChannel = Util.getRandomSubChannel(playerName);
        sendMessage(subChannel, playerName, content);
    }

    @Override
    public void sendMessageToPluginWithSubChannel(String subChannel, String playerName, String content) {
        sendMessage(subChannel, playerName, content);
    }

    @Override
    public CompletableFuture<String> sendMessageAndWaitResponse(String playerName, String demand, String content) {
        String subChannel = Util.getRandomSubChannel(playerName);
        return sendMessageWithResponse(subChannel, playerName, demand, content);
    }

    @Override
    public CompletableFuture<String> sendMessageAndWaitResponseWithSubChannel(String subChannel, String playerName, String demand, String content) {
        return sendMessageWithResponse(subChannel, playerName, demand, content);
    }

    /**
     * Sends a message to the plugin.
     *
     * @param subChannel Bungee SubChannel.
     * @param playerName Player name.
     * @param content   Content for the demand.
     */
    private void sendMessage(String subChannel, String playerName, String content) {
        Optional<Player> playerOptional = Optional.ofNullable(plugin.getServer().getPlayer(playerName));

        if (playerOptional.isPresent()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(subChannel);
            out.writeUTF(content);

            playerOptional.get().getServer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        }
    }

    /**
     * Sends a message to the plugin.
     *
     * @param subChannel Bungee SubChannel.
     * @param playerName Player name.
     * @param demand    Demand to bungee.
     * @param content   Content for the demand.
     */
    private CompletableFuture<String> sendMessageWithResponse(String subChannel, String playerName, String demand, String content) {
        Optional<Player> playerOptional = Optional.ofNullable(plugin.getServer().getPlayer(playerName));
        if (!playerOptional.isPresent()) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete("error");
            return future;
        }
        return sendMessageWithResponse(subChannel, playerOptional.get(), demand, content);
    }

    /**
     * Sends a message to the plugin.
     *
     * @param subChannel Bungee SubChannel.
     * @param player Player name.
     * @param demand    Demand to bungee.
     * @param content   Content for the demand.
     */
    private CompletableFuture<String> sendMessageWithResponse(String subChannel, Player player, String demand, String content) {
        CompletableFuture<String> future = new CompletableFuture<>();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(demand);
        out.writeUTF(content);

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
