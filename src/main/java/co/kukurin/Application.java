package co.kukurin;

import co.kukurin.async.DataSupplier;
import co.kukurin.async.EvernoteExecutors;
import co.kukurin.custom.Optional;
import co.kukurin.editor.EvernoteEditor;
import co.kukurin.environment.ApplicationProperties;
import co.kukurin.gui.AsynchronousScrollableJList;
import co.kukurin.evernote.EvernoteAdapter;
import co.kukurin.evernote.EvernoteEntry;
import co.kukurin.evernote.EvernoteEntryList;
import co.kukurin.gui.JFrameUtils;
import co.kukurin.gui.PredefinedKeyEvents;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.type.Tag;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static co.kukurin.gui.ActionFactory.createAction;
import static java.awt.event.KeyEvent.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

@Slf4j
public class Application extends JFrame {

    // TODO where to put this, and whether to increase size, and whether to have different send/receive executors.
    private static final Executor evernoteCommunicationExecutor = EvernoteExecutors.defaultExecutor; //newSingleThreadExecutor();

    // TODO whether to remove applicationProperties as a member variable
    private final ApplicationProperties applicationProperties;
    private final EvernoteAdapter evernoteAdapter;
    private final PredefinedKeyEvents predefinedKeyEvents;

    private EvernoteEditor contentEditor;
    private AsynchronousScrollableJList<EvernoteEntry> noteJList;
    private JTextField titleTextField;
    private JButton submitNoteButton;
    private CompletableFuture<String> noteContentFetchInProgress;

    Application(EvernoteAdapter evernoteAdapter,
                ApplicationProperties applicationProperties,
                KeyboardFocusManager keyboardFocusManager) {
        this.evernoteAdapter = evernoteAdapter;
        this.applicationProperties = applicationProperties;
        this.predefinedKeyEvents = createPredefinedKeyEvents();

        initWindowFromProperties(this.applicationProperties);
        initGuiElements(this.evernoteAdapter, this.applicationProperties);
        initListeners(keyboardFocusManager);
    }

    private PredefinedKeyEvents createPredefinedKeyEvents() {
        PredefinedKeyEvents predefinedKeyEvents = new PredefinedKeyEvents();

        Predicate<KeyEvent> isAlt = InputEvent::isAltDown;
        Predicate<KeyEvent> isControl = InputEvent::isControlDown;
        Function<Integer, Predicate<KeyEvent>> keyPressed = keyCode -> (e -> e.getKeyCode() == keyCode);

        predefinedKeyEvents.addKeyEvent(isAlt.and(keyPressed.apply(VK_1)), () -> this.noteJList.requestFocusInWindow());
        predefinedKeyEvents.addKeyEvent(keyPressed.apply(VK_ESCAPE), () -> this.contentEditor.requestFocusInWindow());
        predefinedKeyEvents.addKeyEvent(isControl.and(keyPressed.apply(VK_ENTER)), () -> this.onSubmitNoteClick(null));

        return predefinedKeyEvents;
    }

    private void initWindowFromProperties(ApplicationProperties applicationProperties) {
        JFrameUtils.displayAndAddProperties(this,
                WindowConstants.EXIT_ON_CLOSE,
                applicationProperties.getTitle(),
                applicationProperties.getMinWidth(),
                applicationProperties.getMinHeight());
    }

    private void initGuiElements(EvernoteAdapter evernoteAdapter, ApplicationProperties applicationProperties) {
        this.titleTextField = new JTextField();
        this.contentEditor = new EvernoteEditor();
        this.submitNoteButton = new JButton(createAction("Submit note", this::onSubmitNoteClick));
        this.noteJList = new AsynchronousScrollableJList<>(getNoteListUpdater(evernoteAdapter, applicationProperties.getTags()));
        this.noteJList.addListSelectionListener(this::displayNote);

        setLayout(new BorderLayout(5, 5));
        add(this.titleTextField, BorderLayout.NORTH);
        add(this.noteJList, BorderLayout.WEST);
        add(this.contentEditor, BorderLayout.CENTER);
        add(this.submitNoteButton, BorderLayout.SOUTH);
    }

    private void initListeners(KeyboardFocusManager keyboardFocusManager) {
        keyboardFocusManager.addKeyEventDispatcher(predefinedKeyEvents::eventInvoked);
    }

    private DataSupplier<EvernoteEntry> getNoteListUpdater(EvernoteAdapter evernoteAdapter, Set<String> tagsToInclude) {
        NoteFilter filter = new NoteFilter();
        filter.setTagGuids(evernoteAdapter
                .streamTagsByName(tagsToInclude)
                .map(Tag::getGuid)
                .collect(Collectors.toList()));

        return dataSupplierInfo -> {
            EvernoteEntryList evernoteEntryList = this.evernoteAdapter.findNotes(filter, dataSupplierInfo.getFetchStartIndex(), dataSupplierInfo.getFetchSize());
            return evernoteEntryList.getNotes();
        };
    }

    // TODO check for edit changes on currently active note
    // allow multiple requests
    // also check if it's the same URL we're dealing with as noteFetchInProgress
    private void displayNote(ListSelectionEvent unused) {
        this.noteJList.getSelectedValue().ifPresent(selected -> {
            Optional.ofNullable(this.noteContentFetchInProgress)
                    .ifPresent(this::cancelRequestInProgress);

            this.noteContentFetchInProgress = supplyAsync(() -> this.evernoteAdapter.getNoteContents(selected), evernoteCommunicationExecutor);
            this.noteContentFetchInProgress.thenAccept(noteContents -> {
                    selected.setContent(noteContents);

                    this.titleTextField.setText(selected.getTitle());
                    this.contentEditor.setText(selected.getContent());
            });
        });
    }

    private void cancelRequestInProgress(CompletableFuture<String> requestInProgress) {
        final boolean irrelevantValueBecauseIgnoredByImplementation = true;
        requestInProgress.cancel(irrelevantValueBecauseIgnoredByImplementation);
    }

    private void onSubmitNoteClick(ActionEvent unused) {
        String noteTitle = this.titleTextField.getText();
        String noteContent = this.contentEditor.getText();

        this.contentEditor.setText("");
        this.titleTextField.setText("");

        supplyAsync(() -> this.evernoteAdapter.storeNote(noteTitle, noteContent), evernoteCommunicationExecutor)
                .thenAccept(note -> log.info("stored note {}", note));
    }
}
