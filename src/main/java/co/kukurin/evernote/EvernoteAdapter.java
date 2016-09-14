package co.kukurin.evernote;

import co.kukurin.custom.exception.ExceptionHelper;
import co.kukurin.environment.EvernoteProperties;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static co.kukurin.custom.exception.ExceptionHelper.*;

public class EvernoteAdapter {

    private static final int EVERNOTE_METADATA_LENGTH
            = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE en-note SYSTEM " +
            "\"http://xml.evernote.com/pub/enml2.dtd\"><en-note></en-note>").length();

    private final NoteStoreClient noteStoreClient;

    public EvernoteAdapter(NoteStoreClient noteStoreClient) {
        this.noteStoreClient = noteStoreClient;
    }

    public EvernoteEntry updateNote(EvernoteEntry note) {
        return getOrRethrowAsUnchecked(() -> new EvernoteEntry(this.noteStoreClient.updateNote(note.toNote())));
    }

    public EvernoteEntry storeNote(String title, String content) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(addEvernoteMarkupLanguageMetadata(content));

        return getOrRethrowAsUnchecked(() -> mapAsDelegate(this.noteStoreClient.createNote(note)));
    }

    public EvernoteEntry storeIfNonexistentOrUpdate(EvernoteEntry note) {
        return Optional.ofNullable(note.getGuid())
                    .map(noteGuid -> this.updateNote(note))
                    .orElseGet(() -> this.storeNote(note.getTitle(), note.getContent()));
    }

    public EvernoteEntryList findNotes(NoteFilter noteFilter, int startIndex, int fetchSize) {
        return getOrRethrowAsUnchecked(() -> mapAsDelegate(this.noteStoreClient.findNotes(noteFilter, startIndex, fetchSize)));
    }

    private EvernoteEntry mapAsDelegate(Note note) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return new EvernoteEntry(note);
    }

    private EvernoteEntryList mapAsDelegate(NoteList notes) {
        return new EvernoteEntryList(notes);
    }

    public String getNoteContents(EvernoteEntry note) {
        return getOrRethrowAsUnchecked(() -> noteStoreClient.getNoteContent(note.getGuid()));
    }

    public List<Tag> getTagsByName(Set<String> tagNames) {
        return streamTagsByName(tagNames).collect(Collectors.toList());
    }

    public Stream<Tag> streamTagsByName(Set<String> tagNames) {
        return ExceptionHelper.tryGetValue(
                () -> noteStoreClient.listTags()
                        .stream()
                        .filter(tag -> tagNames.contains(tag.getName())))
                .orElseGet(Stream::empty);
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
