package utils.controller.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

public interface FileController {

	public default void saveResource(Plugin plugin, File folder, String resourcePath, boolean replace) {
		if (resourcePath == null || resourcePath.equals(""))
			throw new IllegalArgumentException("ResourcePath cannot be null or empty");
		resourcePath = resourcePath.replace('\\', '/');
		InputStream in = getResource(plugin, resourcePath);
		File outFile = new File(folder, resourcePath);
		int lastIndex = resourcePath.lastIndexOf('/');
		File outDir = new File(folder, resourcePath.substring(0, (lastIndex >= 0) ? lastIndex : 0));
		if (!outDir.exists())
			outDir.mkdirs();
		try {
			if (!outFile.exists() || replace) {
				OutputStream out = new FileOutputStream(outFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);
				out.close();
				in.close();
			} else {
				plugin.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile
						+ " because " + outFile.getName() + " already exists.");
			}
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
		}

	}

	public default InputStream getResource(Plugin plugin, String filename) {
		if (filename == null)
			throw new IllegalArgumentException("Filename cannot be null");
		try {
			URL url = plugin.getClass().getClassLoader().getResource(filename);
			if (url == null)
				return null;
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			return connection.getInputStream();
		} catch (IOException ex) {
			return null;
		}
	}
}
