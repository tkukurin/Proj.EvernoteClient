package co.kukurin.evernote;

import com.evernote.edam.type.Note;
import lombok.experimental.Delegate;

// TODO use instead of Note.
public class EvernoteEntry {

    private @Delegate Note delegate;

    public EvernoteEntry(Note delegate) {
        this.delegate = delegate;
    }

    @Override
    public String toString() {
        return this.delegate.getTitle();
    }

}
