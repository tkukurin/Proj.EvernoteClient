package co.kukurin.evernote;

import co.kukurin.environment.EvernoteProperties;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.type.Note;

import static co.kukurin.custom.exception.ExceptionHelper.*;

public class EvernoteAdapter {

    private static final int EVERNOTE_METADATA_LENGTH
            = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE en-note SYSTEM " +
               "\"http://xml.evernote.com/pub/enml2.dtd\"><en-note></en-note>").length();

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
        note.setContent(addEvernoteMarkupLanguageMetadata(content));

        return getOrRethrowAsUnchecked(() -> this.noteStoreClient.createNote(note));
    }

    private String addEvernoteMarkupLanguageMetadata(String content) {
        return new StringBuilder(content.length() + EVERNOTE_METADATA_LENGTH)
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">")
                .append("<en-note>")
                .append(content)
                .append("</en-note>")
                .toString();
    }

}
