package utils.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

/**
 * Language file driver.
 */
public class LangManager {

	private String fileName;

	private File customConfigFile;

	private Map<String, Object> yamlData;

	private InternalController controller;

	public LangManager(InternalController controller, String filename, File dataFolder) {
		this.fileName = filename;
		this.customConfigFile = new File(dataFolder, this.fileName);
		this.controller = controller;
		if (!this.customConfigFile.exists()) {
			this.customConfigFile.getParentFile().mkdirs();
			controller.saveResource(this.fileName);
		}
		Yaml yaml = new Yaml();
		try {
			this.yamlData = (Map<String, Object>) yaml.load(new FileInputStream(this.customConfigFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String getString(String path) {
		return this.yamlData.get(path).toString().replace("&", "").replace("%minecraft_servername%",
				controller.getConfigManager().getString("server_name"));
	}

	public String getString(Player player, String path) {
		return getString(path).replace("%minecraft_username%", player.getName());
	}

//  public String getString(User user, Player player, String path) {
//    return getString(player, path).replaceAll("%discriminated_discord_name%", 
//        String.valueOf(user.getName()) + "#" + user.getDiscriminator());
//  }
//  
	public long getLong(String path) {
		return Long.parseLong(this.yamlData.get(path).toString());
	}
}