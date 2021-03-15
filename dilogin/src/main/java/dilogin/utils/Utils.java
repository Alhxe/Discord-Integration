package dilogin.utils;

import java.awt.Color;

/**
 * General utilities.
 */
public class Utils {

	/**
	 * Prohibits instantiation of the class.
	 */
	private Utils() {
		throw new IllegalStateException();
	}

	/**
	 * @param colorStr hexadecimal color.
	 * @return Color.
	 */
	public static Color hex2Rgb(String colorStr) {
		return new Color(Integer.valueOf(colorStr.substring(1, 3), 16).intValue(),
				Integer.valueOf(colorStr.substring(3, 5), 16).intValue(),
				Integer.valueOf(colorStr.substring(5, 7), 16).intValue());
	}
}