package co.kukurin.evernote;

import com.evernote.edam.type.Note;
import lombok.Getter;
import lombok.experimental.Delegate;

// TODO use instead of Note.
public class EvernoteEntry {

    private @Delegate Note delegate;
    private @Getter boolean wasModified;

    public EvernoteEntry(Note delegate) {
        this.delegate = delegate;
    }

    @Override
    public String toString() {
        return this.delegate.getTitle();
    }

}
