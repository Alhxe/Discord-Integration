package di.dilogin.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains user information such as name, uuid, etc.
 */
@Getter
@Setter
public class DIUserDto {

    /**
     * Minecraft player name.
     */
    private String playerName;

    /**
     * Minecraft player uuid.
     */
    private String uuid;

    /**
     * Discord user id.
     */
    private String discordId;

}
