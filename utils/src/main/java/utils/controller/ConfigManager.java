package utils.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Configuration file driver.
 */
public class ConfigManager {

	private String fileName;

	private File customConfigFile;

	private Map<String, Object> yamlData;

	public ConfigManager(InternalController controller, String filename, File dataFolder) {
		this.fileName = filename;
		this.customConfigFile = new File(dataFolder, this.fileName);
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
				this.yamlData.get("server_name").toString());
	}

	public long getLong(String path) {
		return Long.parseLong(this.yamlData.get(path).toString());
	}
}