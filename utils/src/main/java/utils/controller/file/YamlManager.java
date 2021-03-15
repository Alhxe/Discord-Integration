package utils.controller.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import utils.controller.PluginController;

/**
 * Language file driver.
 */
public class YamlManager implements FileController {

	/**
	 * Name of the file to be controlled.
	 */
	private String fileName;

	/**
	 * File configuration.
	 */
	private File customConfigFile;

	/**
	 * Map of the data obtained from the yaml.
	 */
	private Map<String, Object> yamlData;

	/**
	 * Plugin controller.
	 */
	PluginController controller;

	@SuppressWarnings("unchecked")
	public YamlManager(PluginController controller, String filename, File dataFolder) {
		this.fileName = filename;
		this.controller = controller;
		this.customConfigFile = new File(dataFolder, this.fileName);
		if (!this.customConfigFile.exists()) {
			this.customConfigFile.getParentFile().mkdirs();
			saveResource(controller.getPlugin(), dataFolder, filename, false);
		}
		Yaml yaml = new Yaml();
		try {
			this.yamlData = (Map<String, Object>) yaml.load(new FileInputStream(this.customConfigFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String getString(String path) {
		return this.yamlData.get(path).toString().replace("&", "§");
	}
}