package di.internal.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Connection DTO.
 */
@Getter
@Setter
public class ConnectionDto {

    /**
     * Server list of BungeeCord.
     */
    private List<String> serverList;

    /**
     * Online count players.
     */
    private int onlinePlayers;

    /**
     * BungeeCord name.
     */
    private String serverName;
}
