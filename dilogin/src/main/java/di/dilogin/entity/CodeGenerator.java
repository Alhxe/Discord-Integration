package di.dilogin.entity;

import di.dicore.DIApi;

/**
 * Code generator for registration.
 */
public class CodeGenerator {

	/**
	 * Prohibits instantiation of the class.
	 */
	private CodeGenerator() {
		throw new IllegalStateException();
	}

	/**
	 * List of valid numbers for the code.
	 */
	private static final String NUMBERS = "0123456789";

	/**
	 * List of valid uppercase letters for the code.
	 */
	private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * List of valid lowercase letters for the code.
	 */
	private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";

	/**
	 * @param length Number of characters for the code.
	 * @return Generated code.
	 */
	public static final String getCode(int length, DIApi api) {
		if(api.getInternalController().getConfigManager().contains("register_custom_characters"))
			return getCode(api.getInternalController().getConfigManager().getString("register_custom_characters"),length);
		
		return getCode(NUMBERS + CAPITAL_LETTERS + LOWER_CASE, length);
	}

	/**
	 * Generate the code.
	 * 
	 * @param key    Total characters for code generation.
	 * @param length Size that the code will have.
	 * @return Generated code.
	 */
	private static String getCode(String key, int length) {
		String code = "";
		for (int i = 0; i < length; i++) {
			code += (key.charAt((int) (Math.random() * key.length())));
		}
		return code;
	}
}
