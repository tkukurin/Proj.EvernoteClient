package co.kukurin;

import co.kukurin.custom.exception.ExceptionHelper;
import co.kukurin.custom.properties.PropertyLoader;
import co.kukurin.environment.ApplicationProperties;
import co.kukurin.environment.EvernoteProperties;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.IOException;

@Slf4j
public class ApplicationStartup {

    public static void main(String[] args) {
        ExceptionHelper
                .usingExceptionHandler(e -> log.error("failed to start application", e))
                .tryExecute(() -> {
                    EvernoteProperties evernoteProperties = initProperties(EvernoteProperties.class, "evernote.properties");
                    ApplicationProperties applicationProperties = initProperties(ApplicationProperties.class, "application.properties");
                    log.info("initialized application");

                    SwingUtilities.invokeAndWait(() -> new Application(evernoteProperties, applicationProperties));
                });
    }

    private static <T> T initProperties(Class<T> classForWhichToLoadProperties, String fromFile) throws IOException, IllegalAccessException, InstantiationException {
        return PropertyLoader
                .forClass(classForWhichToLoadProperties)
                .initFromSystemResourceFiles(fromFile);
    }
}
