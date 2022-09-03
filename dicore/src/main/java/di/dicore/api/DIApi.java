package di.dicore.api;

import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.entity.DiscordCommand;

public interface DIApi {
    CoreController getCoreController();

    InternalController getInternalController();

    void registerDiscordEvent(Object listener);

    void registerDiscordCommand(DiscordCommand command);

}
