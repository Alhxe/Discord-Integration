package di.dilogin.minecraft.bukkit.event.custom;

import di.dilogin.entity.DIUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;

/**
 * This event is generated when logging into DILogin
 */
@Getter
public class DILoginEvent extends Event {

	/**
	 * Default handler list.
	 * @return the handler list.
	 */
	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	/**
	 * The handler list.
	 */
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * The user who is logging in.
	 */
	private DIUser user;

	/**
	 * Get the handlers of this event.
	 * @return the handlers of this event.
	 */
	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	/**
	 * Constructor.
	 * @param user the user who is logging in.
	 */
	public DILoginEvent(DIUser user) {
		this.user = user;
	}
}