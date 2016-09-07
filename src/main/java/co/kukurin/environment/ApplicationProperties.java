package co.kukurin.environment;

import co.kukurin.custom.properties.NotNull;
import lombok.Getter;

@Getter
@NotNull
public class ApplicationProperties {

    private String title;
    private Integer minWidth;
    private Integer minHeight;

}
