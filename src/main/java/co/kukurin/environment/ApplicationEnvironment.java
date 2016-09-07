package co.kukurin.environment;

import co.kukurin.custom.exception.ExceptionHelper;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ApplicationEnvironment {

    private final NoteStoreClient noteStoreClient;

    public ApplicationEnvironment(EvernoteProperties evernoteProperties) {
        this.noteStoreClient = initNoteStoreClient(EvernoteService.SANDBOX, evernoteProperties.getEvernoteToken());
    }

    private NoteStoreClient initNoteStoreClient(EvernoteService evernoteService, String evernoteToken) {
        EvernoteAuth evernoteAuth = new EvernoteAuth(evernoteService, evernoteToken);
        ClientFactory clientFactory = new ClientFactory(evernoteAuth);

        return ExceptionHelper.getOrRethrowAsUnchecked(clientFactory::createNoteStoreClient);
    }

}
