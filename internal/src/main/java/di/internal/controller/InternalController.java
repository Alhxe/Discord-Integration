package di.internal.controller;

import java.util.concurrent.CompletableFuture;

/**
 * Internal controller of the plugin.
 */
public interface InternalController extends BasicController {

    /**
     * @return Plugin channel controller.
     */
    ChannelController getChannelController();

    /**
     * Send a message to the plugin and wait for a response.
     *
     * @return the future connection if is established.
     */
    CompletableFuture<String> initConnectionWithBungee();
}
