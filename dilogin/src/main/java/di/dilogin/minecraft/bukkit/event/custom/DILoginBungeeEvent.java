package di.dilogin.minecraft.bukkit.event.custom;

import di.dilogin.entity.DIUser;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Event;

/**
 * This event is generated when logging into DILogin
 */
@Getter
public class DILoginBungeeEvent extends Event {

	/**
	 * The user who is logging in.
	 */
	private DIUser user;

	/**
	 * Constructor.
	 * @param user the user who is logging in.
	 */
	public DILoginBungeeEvent(DIUser user) {
		this.user = user;
	}
}