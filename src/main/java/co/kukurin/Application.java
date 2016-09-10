package co.kukurin;

import co.kukurin.environment.ApplicationProperties;
import co.kukurin.evernote.AsynchronousScrollableJList;
import co.kukurin.evernote.EvernoteAdapter;
import co.kukurin.evernote.ListWithAsyncMetadata;
import co.kukurin.gui.JFrameUtils;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static co.kukurin.gui.ActionFactory.createAction;
import static co.kukurin.gui.ListenerFactory.createResizeListener;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j
public class Application extends JFrame {

    private final ApplicationProperties applicationProperties;
    private final EvernoteAdapter evernoteAdapter;

    private JTextField titleTextField;
    private JTextArea contentTextArea;
    private JButton submitNoteButton;
    private AsynchronousScrollableJList<Note> noteJList;
    private int fetchSize;

    public Application(EvernoteAdapter evernoteAdapter,
                       ApplicationProperties applicationProperties) {
        this.evernoteAdapter = evernoteAdapter;
        this.applicationProperties = applicationProperties;
        this.fetchSize = applicationProperties.getFetchSize();

        initWindowFromProperties();
        initGuiElements();
        initListeners();
    }

    private void initWindowFromProperties() {
        JFrameUtils.displayAndAddProperties(this,
                WindowConstants.EXIT_ON_CLOSE,
                applicationProperties.getTitle(),
                applicationProperties.getMinWidth(),
                applicationProperties.getMinHeight());
    }

    private void initGuiElements() {
        this.titleTextField = new JTextField();

        this.contentTextArea = new JTextArea();
        JScrollPane contentContainer = new JScrollPane(this.contentTextArea);

        this.submitNoteButton = new JButton(createAction("Submit note", this::onSubmitNoteClick));

        BiFunction<Integer, Integer, List<Note>> noteSupplier = getNoteSupplier();
        ListWithAsyncMetadata<Note> listWithAsyncMetadata = new ListWithAsyncMetadata<>(noteSupplier.apply(0, 5));
        this.noteJList = new AsynchronousScrollableJList<>(listWithAsyncMetadata, Note::getTitle, noteSupplier);


        setLayout(new BorderLayout(5, 5));
        add(this.titleTextField, BorderLayout.NORTH);
        add(this.noteJList, BorderLayout.WEST);
        add(contentContainer, BorderLayout.CENTER);
        add(this.submitNoteButton, BorderLayout.SOUTH);
    }

    public BiFunction<Integer, Integer, List<Note>> getNoteSupplier() {
        NoteFilter filter = new NoteFilter();
        filter.setTagGuids(evernoteAdapter
                .streamTagsByName(applicationProperties.getTags())
                .map(Tag::getGuid)
                .collect(Collectors.toList()));

//        return (startIndex, fetchSize) -> {
//            log.debug("sent fetch request to Evernote: from {}, size {}", startIndex, fetchSize);
//            return evernoteAdapter.findNotes(filter, startIndex, fetchSize).getNotes();
//        };

        return (startIndex, fetchSize) -> {
            log.info("requested fetch with size {} and start index {}", fetchSize, startIndex);
            return this.mock__createListOfSize(startIndex, fetchSize);
        };
    }

    private List<Note> mock__createListOfSize(Integer startIndex, Integer fetchSize) {
        List<Note> notes = new ArrayList<>();
        for(int i = startIndex; i < startIndex + fetchSize; i++) {
            Note n = new Note();
            n.setTitle("note " + i);
            notes.add(n);
        }

        return notes;
    }

    private void initListeners() {
        this.addComponentListener(createResizeListener(this::updateFetchSizeOnResize));
        //this.noteJList.addListSelectionListener(this::displayNote);
    }

    // TODO check for changes
//    private void displayNote(ListSelectionEvent listSelectionEvent) {
//        int selectedNoteIndex = listSelectionEvent.getFirstIndex();
//        Note selected = this.evernoteListModel.getNoteAt(selectedNoteIndex);
//
//        this.titleTextField.setText(selected.getTitle());
//        this.contentTextArea.setText(selected.getContent());
//
//        log.debug(selected.getContent());
//    }

    private void updateFetchSizeOnResize(ComponentEvent componentEvent) {
        //this.fetchSize = this.noteJList.getLastVisibleIndex() - this.noteJList.getFirstVisibleIndex() + 1;
        log.info("listener invoked. " + this.fetchSize);
    }

    private void onSubmitNoteClick(ActionEvent event) {
        String noteTitle = this.titleTextField.getText();
        String noteContent = this.contentTextArea.getText();
        this.contentTextArea.setText("");
        this.titleTextField.setText("");

        supplyAsync(() -> this.evernoteAdapter.storeNote(noteTitle, noteContent))
                .thenAccept(note -> log.info("stored note {}", note));
    }
}
