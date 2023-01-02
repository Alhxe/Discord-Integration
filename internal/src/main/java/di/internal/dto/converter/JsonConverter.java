package di.internal.dto.converter;

import com.google.gson.Gson;

/**
 * Transform the json into dto.
 */
public class JsonConverter<i> {

    /**
     * Main gson object.
     */
    private final Gson gson = new Gson();

    /**
     * The class to transform.
     */
    private final Class<?> clazz;

    /**
     * Constructor.
     * @param clazz The class to transform.
     */
    public JsonConverter(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Convert the json into dto.
     *
     * @param json json to convert.
     * @return dto converted.
     */
    @SuppressWarnings("unchecked")
	public i getDto(String json) {
        return (i) gson.fromJson(json, clazz);
    }

    /**
     * Convert the dto into json.
     *
     * @param dto dto to convert.
     * @return json converted.
     */
    public String getJson(i dto) {
        return gson.toJson(dto);
    }
}
