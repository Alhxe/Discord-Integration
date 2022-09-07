package di.internal.controller.file;

import java.io.*;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import di.internal.controller.PluginController;
import di.internal.utils.Util;

/**
 * Language file driver.
 */
public class YamlManager implements FileController {

	/**
	 * Name of the file to be controlled.
	 */
	private final String fileName;

	/**
	 * File configuration.
	 */
	private final File customConfigFile;

	/**
	 * Map of the data obtained from the yaml.
	 */
	private final Map<String, Object> yamlData;

	/**
	 * Main constructor.
	 * 
	 * @param controller  Plugin controller.
	 * @param filename    The name of the file.
	 * @param dataFolder  The folder where it is located.
	 * @param classLoader Class loader.
	 */
	public YamlManager(PluginController controller, String filename, File dataFolder, ClassLoader classLoader) {
		this.fileName = filename;
		this.customConfigFile = new File(dataFolder, this.fileName);
		if (!this.customConfigFile.exists()) {
			this.customConfigFile.getParentFile().mkdirs();
			saveResource(controller, dataFolder, filename, classLoader, false);
		}

		this.yamlData = getYamlContent(classLoader);
	}

	/**
	 * @param path The value you want to obtain.
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
	private Map<String, Object> getYamlContent(ClassLoader classLoader) {
		try {
			InputStream file = Util.getFileFromResourceAsStream(classLoader, fileName);
			Map<String, Object> custom = new Yaml()
					.load(new FileInputStream(this.customConfigFile));
			Map<String, Object> original = new Yaml().load(file);
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