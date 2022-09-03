package di.internal.controller.file;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import di.internal.controller.PluginController;

public interface FileController {

	public default void saveResource(PluginController controller, File folder, String resourcePath, boolean replace) {
		if (resourcePath == null || resourcePath.equals(""))
			throw new IllegalArgumentException("ResourcePath cannot be null or empty");
		resourcePath = resourcePath.replace('\\', '/');
		InputStream in = getResource(controller, resourcePath);
		File outFile = new File(folder, resourcePath);
		int lastIndex = resourcePath.lastIndexOf('/');
		File outDir = new File(folder, resourcePath.substring(0, (lastIndex >= 0) ? lastIndex : 0));
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

	public default InputStream getResource(PluginController controller, String filename) {
		if (filename == null)
			throw new IllegalArgumentException("Filename cannot be null");
		try {
			URL url = controller.getClass().getClassLoader().getResource(filename);
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
