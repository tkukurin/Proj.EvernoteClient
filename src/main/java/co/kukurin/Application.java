package co.kukurin;

import co.kukurin.environment.ApplicationProperties;
import co.kukurin.evernote.EvernoteAdapter;
import co.kukurin.evernote.EvernoteListModel;
import co.kukurin.gui.JFrameUtils;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
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
    private EvernoteListModel evernoteListModel;
    private JList<String> noteJList;
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
        this.submitNoteButton = new JButton(createAction("Submit note", this::onSubmitNoteClick));
        this.evernoteListModel = new EvernoteListModel(getNoteSupplier(), () -> this.fetchSize);
        this.noteJList = new JList<>(this.evernoteListModel);

        JScrollPane noteListContainer = new JScrollPane(this.noteJList);
        JScrollPane contentContainer = new JScrollPane(this.contentTextArea);

        setLayout(new BorderLayout(5, 5));
        add(this.titleTextField, BorderLayout.NORTH);
        add(noteListContainer, BorderLayout.WEST);
        add(contentContainer, BorderLayout.CENTER);
        add(this.submitNoteButton, BorderLayout.SOUTH);
        //pack();
    }

    public BiFunction<Integer,Integer,NoteList> getNoteSupplier() {
        NoteFilter filter = new NoteFilter();
        filter.setTagGuids(evernoteAdapter
                .streamTagsByName(applicationProperties.getTags())
                .map(Tag::getGuid)
                .collect(Collectors.toList()));

        return (startIndex, fetchSize) -> {
            log.debug("sent fetch request to Evernote: from {}, size {}", startIndex, fetchSize);
            return evernoteAdapter.findNotes(filter, startIndex, fetchSize);
        };

//        return (startIndex, fetchSize) -> {
//            log.info("requested fetch with size {} and start index {}", fetchSize, startIndex);
//            return new NoteList(startIndex, 20, this.mock__createListOfSize(startIndex, fetchSize));
//        };
    }

    private void initListeners() {
        this.addComponentListener(createResizeListener(this::updateFetchSizeOnResize));
        this.noteJList.addListSelectionListener(this::displayNote);
    }

    // TODO check for changes
    private void displayNote(ListSelectionEvent listSelectionEvent) {
        int selectedNoteIndex = listSelectionEvent.getFirstIndex();
        Note selected = this.evernoteListModel.getNoteAt(selectedNoteIndex);

        this.titleTextField.setText(selected.getTitle());
        this.contentTextArea.setText(selected.getContent());

        log.debug(selected.getContent());
    }

    private void updateFetchSizeOnResize(ComponentEvent componentEvent) {
        this.fetchSize = this.noteJList.getLastVisibleIndex() - this.noteJList.getFirstVisibleIndex() + 1;
        log.info("listener invoked. " + this.fetchSize);
    }

    private List<Note> mock__createListOfSize(int startIndex, int size) {
        List<Note> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(mock__noteWithTitle("title " + (startIndex + i)));
        }
        return list;
    }

    private Note mock__noteWithTitle(String title) {
        Note n = new Note();
        n.setTitle(title);
        return n;
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
