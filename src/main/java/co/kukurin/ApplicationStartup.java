package co.kukurin;

import co.kukurin.custom.exception.ExceptionHelper;
import co.kukurin.custom.properties.PropertyLoader;
import co.kukurin.environment.ApplicationProperties;
import co.kukurin.environment.EvernoteProperties;
import co.kukurin.evernote.EvernoteAdapter;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.IOException;

import static co.kukurin.custom.exception.ExceptionHelper.getOrRethrowAsUnchecked;

@Slf4j
public class ApplicationStartup {

    public static void main(String[] args) {
        ExceptionHelper
                .usingExceptionHandler(e -> log.error("failed to start application", e))
                .tryExecute(() -> {
                    EvernoteProperties evernoteProperties = initProperties(EvernoteProperties.class, "evernote.properties");
                    ApplicationProperties applicationProperties = initProperties(ApplicationProperties.class, "application.properties");
                    EvernoteAdapter evernoteAdapter = getEvernoteAdapter(evernoteProperties);
                    log.info("initialized application");

                    SwingUtilities.invokeLater(() -> new Application(evernoteAdapter, applicationProperties));
                });
    }

    private static EvernoteAdapter getEvernoteAdapter(EvernoteProperties evernoteProperties) {
        String token = evernoteProperties.getEvernoteToken();
        EvernoteService evernoteService = EvernoteService.SANDBOX;

        EvernoteAuth evernoteAuth = new EvernoteAuth(evernoteService, token);
        ClientFactory clientFactory = new ClientFactory(evernoteAuth);

        return new EvernoteAdapter(getOrRethrowAsUnchecked(clientFactory::createNoteStoreClient));
    }

    private static <T> T initProperties(Class<T> classForWhichToLoadProperties, String fromFile) throws IOException, IllegalAccessException, InstantiationException {
        return PropertyLoader
                .forClass(classForWhichToLoadProperties)
                .initFromSystemResourceFiles(fromFile);
    }
}
