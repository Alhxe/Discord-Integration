package di.dilogin.entity;

import di.dicore.DIApi;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Random;

/**
 * Code generator for registration.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodeGenerator {

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
	 * Random generator.
	 */
	private static Random random = new Random();

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
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < length; i++) {
			code.append(key.charAt(random.nextInt(key.length())));
		}
		return code.toString();
	}
}
