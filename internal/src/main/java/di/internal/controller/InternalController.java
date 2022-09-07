package di.internal.controller;

public interface InternalController extends BasicController {

    /**
     * @return Plugin channel controller.
     */
    ChannelController getChannelController();
}
