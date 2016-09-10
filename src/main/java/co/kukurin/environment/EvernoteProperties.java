package co.kukurin.environment;

import co.kukurin.custom.properties.NotNull;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@Getter
@NotNull
public class EvernoteProperties {

    private String evernoteToken;

}
