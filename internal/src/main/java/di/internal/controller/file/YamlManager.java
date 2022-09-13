package di.internal.controller.file;

import java.io.*;
import java.util.Map;

import di.internal.controller.PluginController;

/**
 * Language file driver.
 */
public class YamlManager implements FileController {

    /**
     * Map of the data obtained from the yaml.
     */
    private Map<String, Object> yamlData;

    /**
     * Main constructor. It initializes the file and loads the data. If the data comes
     * from the plugin located in BungeeCord, the default local file will be temporarily
     * loaded into memory, to later be replaced by the information that comes from BungeeCord.
     *
     * @param controller     Plugin controller.
     * @param filename       The name of the file.
     * @param dataFolder     The folder where it is located.
     * @param classLoader    Class loader.
     * @param isDataInBungee If the data is in bungee.
     */
    public YamlManager(PluginController controller, String filename, File dataFolder, ClassLoader classLoader, boolean isDataInBungee) {
        if (isDataInBungee) {
            File customConfigFile = new File(dataFolder, filename);
            if (customConfigFile.exists()) {
                customConfigFile.getParentFile().mkdirs();
                saveResource(controller, dataFolder, filename, classLoader, false);
                this.yamlData = getYamlContent(filename, customConfigFile, classLoader);
            }
        } else {
            this.yamlData = getOriginalYamlContent(filename, classLoader);
        }
    }

    /**
     * @param path The value you want to obtain.
     * @return The content of the sought value.
     */
    public String getString(String path) {
        try {
            char specialChar = (char) 167;
            return yamlData.get(path).toString().replace('&', specialChar);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> getMap() {
        return this.yamlData;
    }

    @Override
    public void setData(Map<String, Object> data) {
        this.yamlData = data;
    }
}