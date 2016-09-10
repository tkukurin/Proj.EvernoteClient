package co.kukurin.evernote;

import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;

import javax.swing.*;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

// TODO async update
public class EvernoteListModel extends AbstractListModel<String> {
    private static final Note EMPTY_NOTE = new Note();
    private static final int NUM_ITEMS_TO_LOAD_AROUND_CURRENTLY_VISIBLE = 5;

    private final BiFunction<Integer, Integer, NoteList> noteSupplier;
    private final Supplier<Integer> numberOfVisibleListItemsSupplier;
    private NoteList noteList;

    public EvernoteListModel(BiFunction<Integer, Integer, NoteList> noteSupplierFromStartIndexAndFetchSize,
                             Supplier<Integer> numberOfVisibleListItemsSupplier) {
        this.noteSupplier = noteSupplierFromStartIndexAndFetchSize;
        this.numberOfVisibleListItemsSupplier = numberOfVisibleListItemsSupplier;
    }

    @Override
    public int getSize() {
        if(this.noteList == null)
            this.noteList = loadNewNotesWithBuffer(0);

        return noteList.getTotalNotes();
    }

    @Override
    public String getElementAt(int index) {
        return getNoteAt(index).getTitle();
    }

    // TODO should be AbstractListModel<Note>, and then list renderer extracts string.
    public Note getNoteAt(int index) {
        return getRelativeToTotalNotes(index)
                .orElseGet(() -> loadNewBatchAndGetNoteAt(index));
    }

    private Optional<Note> getRelativeToTotalNotes(int index) {
        int firstNoteIndex = this.noteList.getStartIndex();
        int nextBatchFirstNoteIndex = firstNoteIndex + this.noteList.getNotesSize();

        return Optional.of(index)
                .filter(i -> i >= firstNoteIndex && i < nextBatchFirstNoteIndex)
                .map(i -> this.noteList.getNotes().get(i - firstNoteIndex));
                //.map(Note::getTitle);
    }

    private Note loadNewBatchAndGetNoteAt(int index) {
        this.noteList = loadNewNotesWithBuffer(index);

        return Optional.of(this.noteList)
                .filter(list -> list.getNotesSize() > 0)
                .map(list -> list.getNotes().get(0))
                .get();
                //.orElse(EMPTY_NOTE.getTitle());
    }

    private NoteList loadNewNotesWithBuffer(int startIndex) {
        int firstLoadIndex = Math.max(0, startIndex - NUM_ITEMS_TO_LOAD_AROUND_CURRENTLY_VISIBLE);
        int lastLoadIndex = numberOfVisibleListItemsSupplier.get() + NUM_ITEMS_TO_LOAD_AROUND_CURRENTLY_VISIBLE;

        return noteSupplier.apply(firstLoadIndex, lastLoadIndex);
    }

}
