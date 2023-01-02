package di.dilogin.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains session information.
 */
@Getter
@Setter
public class SessionDto {
	String playerName;
	String ip;
	boolean isValid;
}
