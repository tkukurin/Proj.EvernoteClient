package co.kukurin;

import co.kukurin.async.AsynchronousJList;
import co.kukurin.async.AsynchronousUpdater;
import co.kukurin.async.DataSupplier;
import co.kukurin.async.EvernoteExecutors;
import co.kukurin.custom.Optional;
import co.kukurin.editor.EvernoteEditor;
import co.kukurin.environment.ApplicationProperties;
import co.kukurin.evernote.EvernoteAdapter;
import co.kukurin.evernote.EvernoteEntry;
import co.kukurin.evernote.EvernoteEntryList;
import co.kukurin.utils.ComponentUtils;
import co.kukurin.utils.JFrameUtils;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.type.Tag;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static co.kukurin.utils.ActionFactory.createAction;
import static java.awt.BorderLayout.*;
import static java.awt.event.KeyEvent.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static javax.swing.JOptionPane.*;

@Slf4j
public class Application extends JFrame {

    // TODO where to put this, and whether to increase size, and whether to have different send/receive executors.
    private static final Executor evernoteCommunicationExecutor = EvernoteExecutors.defaultExecutor;

    private final EvernoteAdapter evernoteAdapter;
    private final ShortcutResponders shortcutResponders;
    private final AsynchronousUpdater<Collection<EvernoteEntry>> listToServerSynchronizer;
    private CompletableFuture<String> noteContentFetchInProgress;

    private EvernoteEditor contentEditor;
    private AsynchronousJList<EvernoteEntry, Collection<EvernoteEntry>> noteJList;

    Application(EvernoteAdapter evernoteAdapter,
                ApplicationProperties applicationProperties,
                KeyboardFocusManager keyboardFocusManager) {
        this.evernoteAdapter = evernoteAdapter;
        this.shortcutResponders = createPredefinedKeyEvents();
        this.listToServerSynchronizer = createListUpdater(evernoteAdapter, applicationProperties,
                notes -> this.noteJList.setModel(new DefaultListModel<>(notes)));

        initWindowFromProperties(applicationProperties);
        initGuiElements(evernoteAdapter, applicationProperties);
        initListeners(keyboardFocusManager);
    }

    private ShortcutResponders createPredefinedKeyEvents() {
        ShortcutResponders shortcutResponders = new ShortcutResponders();

        Predicate<KeyEvent> isAlt = InputEvent::isAltDown;
        Predicate<KeyEvent> isControl = InputEvent::isControlDown;
        Predicate<KeyEvent> isShift = InputEvent::isShiftDown;
        Function<Integer, Predicate<KeyEvent>> keyPressed = keyCode -> (e -> e.getKeyCode() == keyCode);

        shortcutResponders.addKeyEvent(isAlt.and(keyPressed.apply(VK_1)), () -> this.noteJList.requestFocusInWindow());
        shortcutResponders.addKeyEvent(keyPressed.apply(VK_ESCAPE), () -> this.contentEditor.requestFocusInWindow());
        shortcutResponders.addKeyEvent(isControl.and(keyPressed.apply(VK_ENTER)), () -> this.onSubmitNoteClick(null));
        shortcutResponders.addKeyEvent(isControl.and(isShift).and(keyPressed.apply(VK_F)), () -> log.info("find action TODO"));

        return shortcutResponders;
    }

    private AsynchronousUpdater<Collection<EvernoteEntry>> createListUpdater(EvernoteAdapter evernoteAdapter,
                                                                 ApplicationProperties applicationProperties,
                                                                 Consumer<Collection<EvernoteEntry>> dataConsumer) {
        return new AsynchronousUpdater<>(getEvernoteEntrySupplier(evernoteAdapter, applicationProperties.getTags()),
                dataConsumer,
                EvernoteExecutors.defaultExecutor,
                applicationProperties.getFetchSize());
    }

    private void initWindowFromProperties(ApplicationProperties applicationProperties) {
        JFrameUtils.displayAndAddProperties(this,
                WindowConstants.EXIT_ON_CLOSE,
                applicationProperties.getTitle(),
                applicationProperties.getMinWidth(),
                applicationProperties.getMinHeight());
    }

    private void initGuiElements(EvernoteAdapter evernoteAdapter, ApplicationProperties applicationProperties) {
        this.contentEditor = new EvernoteEditor();
        this.noteJList = new AsynchronousJList<>(
                createListUpdater(evernoteAdapter, applicationProperties, notes -> this.noteJList.getModel().addAll(notes)),
                new DefaultListModel<>());
        this.noteJList.addListSelectionListener(this::displayNote);
        JButton submitNoteButton = new JButton(createAction("Submit note", this::onSubmitNoteClick));
        JButton synchronizeButton = new JButton(createAction("Synchronize", this::onSynchronizeClick));
        JPanel syncAndSubmitButton = ComponentUtils
                .createContainerFor(synchronizeButton, submitNoteButton)
                .withLayoutConstraints(LINE_START, LINE_END);

        add(this.noteJList, LINE_START);
        add(this.contentEditor, CENTER);
        add(syncAndSubmitButton, PAGE_END);
    }

    private void initListeners(KeyboardFocusManager keyboardFocusManager) {
        keyboardFocusManager.addKeyEventDispatcher(shortcutResponders::eventInvoked);
    }

    private DataSupplier<Collection<EvernoteEntry>> getEvernoteEntrySupplier(EvernoteAdapter evernoteAdapter, Set<String> tagsToInclude) {
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
    private void displayNote(ListSelectionEvent event) {
        if(event.getValueIsAdjusting()) {
            return;
        }

        if(this.contentEditor.entryWasModified()) {
            int result = showConfirmDialog(this, "Current document was modified. Save to Evernote?");

            if(result != JOptionPane.NO_OPTION) {
                if(result == JOptionPane.YES_OPTION) {
                    log.info("confirmed.");
                }

                return;
            }

        }

        this.noteJList.getSelectedValue().ifPresent(selected -> {
            Optional.ofNullable(this.noteContentFetchInProgress)
                    .ifPresent(this::cancelRequestInProgress);

            this.noteContentFetchInProgress = supplyAsync(() -> this.evernoteAdapter.getNoteContents(selected), evernoteCommunicationExecutor);
            this.noteContentFetchInProgress.thenAccept(noteContents -> {
                    selected.setContent(noteContents);
                    setDisplayedEntry(selected);
            });
        });
    }

    private void cancelRequestInProgress(CompletableFuture<String> requestInProgress) {
        final boolean irrelevantValueBecauseIgnoredByImplementation = true;
        requestInProgress.cancel(irrelevantValueBecauseIgnoredByImplementation);
    }

    private void setDisplayedEntry(EvernoteEntry evernoteEntry) {
        this.contentEditor.setEntry(evernoteEntry);
    }

    private void onSubmitNoteClick(ActionEvent unused) {
        String noteTitle = this.contentEditor.getTitle();
        String noteContent = this.contentEditor.getContent();

        supplyAsync(() -> this.evernoteAdapter.storeNote(noteTitle, noteContent), evernoteCommunicationExecutor)
                .thenAccept(note -> {
                    this.noteJList.getModel().add(0, note);
                    this.noteJList.setSelectedIndex(0);
                });
    }

    // TODO check if current item has been updated.
    private void onSynchronizeClick(ActionEvent unused) {
        this.listToServerSynchronizer.runAsyncUpdate(0);
    }
}
