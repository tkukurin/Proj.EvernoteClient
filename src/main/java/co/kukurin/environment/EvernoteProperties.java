package co.kukurin.environment;

import co.kukurin.custom.properties.NotNull;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class EvernoteProperties {

    @NotNull
    private String evernoteToken;
    private String tags;

}
