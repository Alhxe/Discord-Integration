package di.dilogin.controller.impl;

import di.dilogin.controller.DiscordController;

public class DiscordControllerExternImpl implements DiscordController{

	@Override
	public boolean userHasRole(String roleid, String player) {
		return false;
	}

	@Override
	public boolean serverHasRole(String roleid) {
		return false;
	}

	@Override
	public void giveRole(String roleid, String player, String reason) {
		
	}

	@Override
	public void removeRole(String roleid, String player, String reason) {
		
	}

	@Override
	public boolean isWhiteListed(String player) {
		return false;
	}

}
