package di.internal.controller.file;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import di.internal.controller.PluginController;
import di.internal.utils.Util;
import org.yaml.snakeyaml.Yaml;

/**
 * Interface that manages the files.
 */
public interface FileController {

    /**
     * Saves a resource from the plugin jar to the disk.
     *
     * @param controller   Plugin controller.
     * @param folder       Folder where the resource will be saved.
     * @param resourcePath Path of the resource.
     * @param classLoader  Class loader.
     * @param replace      If the resource should be replaced.
     */
    default void saveResource(PluginController controller, File folder, String resourcePath, ClassLoader classLoader, boolean replace) {
        if (resourcePath == null || resourcePath.equals(""))
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(classLoader, resourcePath);
        File outFile = new File(folder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(folder, resourcePath.substring(0, Math.max(lastIndex, 0)));
        if (!outDir.exists())
            outDir.mkdirs();

        if (!outFile.exists() || replace) {
            try (OutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
                in.close();
            } catch (IOException ex) {
                controller.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
            }
        } else {
            controller.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile
                    + " because " + outFile.getName() + " already exists.");
        }

    }

    /**
     * Gets a resource from the plugin jar.
     *
     * @param classLoader Class loader.
     * @param filename    Name of the resource.
     * @return The resource.
     */
    default InputStream getResource(ClassLoader classLoader, String filename) {
        if (filename == null)
            throw new IllegalArgumentException("Filename cannot be null");
        try {
            URL url = classLoader.getResource(filename);
            if (url == null)
                return null;
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Checks the missing paths in the user's custom file and adds them.
     *
     * @param fileName    The name of the file.
     * @param customFile  The file to be checked.
     * @param classLoader Class Loader.
     * @return Data of the file.
     */
    default Map<String, Object> getYamlContent(String fileName, File customFile, ClassLoader classLoader) {
        try {
            InputStream file = Util.getFileFromResourceAsStream(classLoader, fileName);
            Map<String, Object> custom = new Yaml()
                    .load(new FileInputStream(customFile));
            Map<String, Object> original = new Yaml().load(file);

            if (original == null)
                return Collections.emptyMap();

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
        return Collections.emptyMap();
    }

    /**
     * Get the content of a jar file.
     *
     * @param fileName    The name of the file.
     * @param classLoader Class Loader.
     * @return Data of the file.
     */
    default Map<String, Object> getOriginalYamlContent(String fileName, ClassLoader classLoader) {
        try {
            InputStream file = Util.getFileFromResourceAsStream(classLoader, fileName);

            Map<String, Object> original = new Yaml().load(file);

            if (original == null)
                return Collections.emptyMap();

            file.close();
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    /**
     * Set the content of a file.
     *
     * @param data Data.
     */
    void setData(Map<String, Object> data);

    /**
     * @return All the data.
     */
    Map<String, Object> getMap();
}
