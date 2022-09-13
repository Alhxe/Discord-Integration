package di.internal.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains bot information.
 */
@Getter
@Setter
public class BotDto {

    /**
     * Bot prefix.
     */
    private String prefix;

    /**
     * Main server id.
     */
    private long serverId;
}
