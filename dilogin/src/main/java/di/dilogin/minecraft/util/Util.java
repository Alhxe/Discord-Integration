package di.dilogin.minecraft.util;

import java.util.Optional;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class Util {
	
	private Util() {
		throw new IllegalStateException();
	}
	
	public static boolean isWhiteListed(User user) {
		Optional<Role> optRole = DILoginController.requiredRole();
		if (optRole.isPresent()) {
			Role role = optRole.get();
			Member member = user.getJDA()
					.getGuildById(BukkitApplication.getDIApi().getCoreController().getBot().getServerid())
					.retrieveMember(user, true).complete();
			if (!member.getRoles().contains(role))
				return false;
		}
		return true;
	}

}
