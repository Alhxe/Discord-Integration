package di.internal.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Channel controller.
 */
public interface ChannelController {

    /**
     * Send a message to the plugin.
     *
     * @param playerName Player to send the message.
     * @param message    Message to send.
     */
    void sendMessageToPlugin(String playerName, String message);

    /**
     * Send a message to the plugin.
     *
     * @param subChannel SubChannel to send the message.
     * @param playerName Player to send the message.
     * @param message    Message to send.
     */
    void sendMessageToPlugin(String subChannel, String playerName, String message);

    /**
     * Send a message to the plugin and wait for a response.
     *
     * @param playerName Player to send the message.
     * @param message    Message to send.
     * @return
     */
    CompletableFuture<String> sendMessageAndWaitResponse(String playerName, String message);

    /**
     * Send a message to the plugin and wait for a response.
     *
     * @param subChannel SubChannel to send the message.
     * @param playerName Player to send the message.
     * @param message    Message to send.
     * @return
     */
    CompletableFuture<String> sendMessageAndWaitResponse(String subChannel, String playerName, String message);


}
