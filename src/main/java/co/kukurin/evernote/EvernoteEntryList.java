package co.kukurin.evernote;

import com.evernote.edam.notestore.NoteList;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.stream.Collectors;

public class EvernoteEntryList {

    private final NoteList delegate;

    public EvernoteEntryList(NoteList delegate) {
        this.delegate = delegate;
    }

    public List<EvernoteEntry> getNotes() {
        return delegate.getNotes().stream().map(EvernoteEntry::new).collect(Collectors.toList());
    }

}
