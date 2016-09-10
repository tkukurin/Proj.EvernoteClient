package co.kukurin.environment;

import co.kukurin.custom.properties.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@NotNull
public class ApplicationProperties {

    private String title;
    private Integer minWidth;
    private int minHeight;

    // TODO either only evernoteProperties or appplicationProperties should have this.
    private Integer fetchSize;

    @Setter
    private Set<String> tags;

}
