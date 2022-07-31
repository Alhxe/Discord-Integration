package di.internal.controller.file;

import java.io.*;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import di.internal.controller.PluginController;
import di.internal.utils.Utils;

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

	/**
	 * Main constructor.
	 * 
	 * @param controller  Plugin driver
	 * @param filename    The name of the file
	 * @param dataFolder  The folder where it is located
	 * @param classLoader Class loader.
	 */
	public YamlManager(PluginController controller, String filename, File dataFolder, ClassLoader classLoader) {
		this.fileName = filename;
		this.controller = controller;
		this.customConfigFile = new File(dataFolder, this.fileName);
		if (!this.customConfigFile.exists()) {
			this.customConfigFile.getParentFile().mkdirs();
			saveResource(controller.getPlugin(), dataFolder, filename, false);
		}

		this.yamlData = getYamlContent(classLoader);

	}

	/**
	 * @param path The value you want to obtain
	 * @return The content of the sought value.
	 */
	public String getString(String path) {
		try {
			char specialChar = (char)167;
			return yamlData.get(path).toString().replace('&', specialChar);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks the missing paths in the user's custom file and adds them.
	 * 
	 * @param classLoader Class Loader.
	 * @return Custom file completed.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getYamlContent(ClassLoader classLoader) {
		try {
			InputStream file = Utils.getFileFromResourceAsStream(classLoader, fileName);
			Map<String, Object> custom = (Map<String, Object>) new Yaml()
					.load(new FileInputStream(this.customConfigFile));
			Map<String, Object> original = (Map<String, Object>) new Yaml().load(file);
			if (original==null)
				return null;
			
			original.forEach((path, content) -> {
				if (!custom.containsKey(path))
					custom.put(path, content);
			});
			original.clear();
			file.close();
			return custom;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}