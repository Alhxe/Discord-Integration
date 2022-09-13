package di.internal.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Contains file information such as config.yml
 */
@Getter
@Setter
public class FileDto {

    /**
     * Data from file.
     */
    private Map<String, Object> yamlData;

}
