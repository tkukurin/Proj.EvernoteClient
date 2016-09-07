package co.kukurin.evernote;

import co.kukurin.environment.EvernoteProperties;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.type.Note;

import static co.kukurin.custom.exception.ExceptionHandler.*;

public class EvernoteAdapter {

    private final NoteStoreClient noteStoreClient;

    public EvernoteAdapter(EvernoteProperties evernoteProperties) {
        this.noteStoreClient = initNoteStoreClient(EvernoteService.SANDBOX, evernoteProperties.getEvernoteToken());
    }

    private NoteStoreClient initNoteStoreClient(EvernoteService evernoteService, String evernoteToken) {
        EvernoteAuth evernoteAuth = new EvernoteAuth(evernoteService, evernoteToken);
        ClientFactory clientFactory = new ClientFactory(evernoteAuth);

        return getOrRethrowAsUnchecked(clientFactory::createNoteStoreClient);
    }

    public Note storeNote(String title, String content) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);

        return getOrRethrowAsUnchecked(() -> this.noteStoreClient.createNote(note));
    }

}
