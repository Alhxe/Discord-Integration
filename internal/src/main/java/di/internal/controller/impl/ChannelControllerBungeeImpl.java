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
     * @param content    Content for the demand.
     */
    private void sendMessage(String subChannel, String playerName, String content) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(content);


        Optional<ProxiedPlayer> playerOptional = networkPlayers.stream().findAny();

        playerOptional.ifPresent(proxiedPlayer -> proxiedPlayer.getServer().getInfo().sendData("BungeeCord", out.toByteArray()));
    }

    /**
     * Sends a message to the plugin.
     *
     * @param subChannel Bungee SubChannel.
     * @param playerName Player name.
     * @param demand     Demand to bungee.
     * @param content    Content for the demand.
     */
    private CompletableFuture<String> sendMessageWithResponse(String subChannel, String playerName, String demand, String content) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            future.complete("error");
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(demand);
        out.writeUTF(content);

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
