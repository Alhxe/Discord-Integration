package di.dilogin.controller.file;

import di.dilogin.controller.MainController;
import di.internal.controller.file.YamlManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Command alias controller.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandAliasController {

	/**
	 * Main alias file manager.
	 */
	private static final YamlManager manager = MainController.getDIApi().getInternalController().getFile("alias");

	/**
	 * Get command alias from path.
	 * 
	 * @param string Path.
	 * @return Command alias.
	 */
	public static String getAlias(String string) {
		return manager.getString(string);
	}
}
