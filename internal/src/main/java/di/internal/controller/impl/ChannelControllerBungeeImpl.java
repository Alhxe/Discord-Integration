package di.internal.controller.impl;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import di.internal.controller.ChannelController;
import di.internal.interceptor.ChannelBungeeInterceptor;
import di.internal.utils.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Class that manages the channel events.
 */
public class ChannelControllerBungeeImpl implements ChannelController {

    /**
     * Bungee plugin.
     */
    private final Plugin plugin;

    /**
     * Bungee channel message interceptor.
     */
    private final ChannelBungeeInterceptor channelBungeeInterceptor;

    /**
     * Main Class Constructor.
     *
     * @param plugin Bukkit plugin.
     */
    public ChannelControllerBungeeImpl(Plugin plugin) {
        this.plugin = plugin;
        this.channelBungeeInterceptor = new ChannelBungeeInterceptor();
        plugin.getProxy().registerChannel("BungeeCord");
        plugin.getProxy().getPluginManager().registerListener(plugin, channelBungeeInterceptor);
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
    private void sendMessage(String subChannel, String playerName, String message) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(message);

        Optional<ProxiedPlayer> playerOptional = networkPlayers.stream().findAny();

        playerOptional.ifPresent(proxiedPlayer -> proxiedPlayer.getServer().getInfo().sendData("BungeeCord", out.toByteArray()));
    }

    /**
     * Sends a message to the plugin and waits for a response.
     *
     * @param subChannel Bungee SubChannel.
     * @param playerName Player name.
     * @param message    Message to send.
     * @return CompletableFuture with the response.
     */
    private CompletableFuture<String> sendMessageWithResponse(String subChannel, String playerName, String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            future.complete("error");
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(message);

        assert networkPlayers != null;
        Optional<ProxiedPlayer> playerOptional = networkPlayers.stream().filter(u -> u.getDisplayName().equals(playerName))
                .findFirst();

        if (!playerOptional.isPresent())
            future.complete("error");

        ProxiedPlayer player = playerOptional.get();
        player.getServer().getInfo().sendData("BungeeCord", out.toByteArray());
        channelBungeeInterceptor.addMessage(subChannel);

        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            boolean isFinished = false;
            for (int i = 0; !isFinished && i <= 10; i++) {
                if (channelBungeeInterceptor.containsMessage(subChannel + "answer")) {
                    future.complete(channelBungeeInterceptor.getMessage(subChannel + "answer"));
                    isFinished = true;
                }
            }
            if (!isFinished) {
                future.complete("error");
            }
            channelBungeeInterceptor.removeMessage(subChannel);
        }, 1, 1, TimeUnit.SECONDS);
        return future;
    }
}
