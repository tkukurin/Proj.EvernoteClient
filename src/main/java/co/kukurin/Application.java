package co.kukurin;

import co.kukurin.custom.properties.PropertyLoader;
import co.kukurin.environment.ApplicationEnvironment;
import co.kukurin.environment.ApplicationProperties;
import co.kukurin.environment.EvernoteProperties;
import co.kukurin.evernote.EvernoteAdapter;
import co.kukurin.gui.JFrameUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.IOException;

@Slf4j
public class Application extends JFrame {

    private final ApplicationProperties applicationProperties;
    private final EvernoteAdapter evernoteAdapter;

    public Application(EvernoteProperties evernoteProperties,
                       ApplicationProperties applicationProperties) {
        this.evernoteAdapter = new EvernoteAdapter(evernoteProperties);
        this.applicationProperties = applicationProperties;

        JFrameUtils.initWindowProperties(this);
    }

    public static void main(String[] args) {
        try {
            EvernoteProperties evernoteProperties = initProperties(EvernoteProperties.class, "evernote.properties");
            ApplicationProperties applicationProperties = initProperties(ApplicationProperties.class, "application.properties");

            log.info("initialized application");
            SwingUtilities.invokeAndWait(() -> new Application(evernoteProperties, applicationProperties));
        } catch (Exception e) {
            log.error("failed to start application", e);
        }
    }

    private static <T> T initProperties(Class<T> clazz, String fromFile) throws IOException, IllegalAccessException, InstantiationException {
        return PropertyLoader
                .forClass(clazz)
                .initFromSystemResourceFiles(fromFile);
    }

}
