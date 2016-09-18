package co.kukurin.editor;

import co.kukurin.evernote.EvernoteEntry;
import com.evernote.edam.type.Note;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

import static co.kukurin.utils.DocumentListenerFactory.*;
import static java.awt.BorderLayout.*;

public class EvernoteEditor extends JPanel {
    // TODO move this somewhere in properties.
    private static final Font editorFont = new Font("Consolas", Font.PLAIN, 14);

    private EvernoteEntry activeEntry;
    private final JScrollPane scrollPane;
    private final JTextField textField;

    public EvernoteEditor() {
        JTextArea textArea = new JTextArea();

        this.activeEntry = new EvernoteEntry(new Note());
        this.scrollPane = new JScrollPane(textArea);
        this.textField = new JTextField();

        textArea.setFont(editorFont);
        this.textField.setFont(editorFont);

        setLayout(new BorderLayout(0, 5));
        add(this.textField, PAGE_START);
        add(this.scrollPane, CENTER);

        initListeners(textArea, this.textField);
    }

    public void setEntry(EvernoteEntry entry) {
        this.activeEntry = entry;
        this.textField.setText(entry.getTitle());
        this.getView().setText(entry.getContent());

        // sneaky change, find different way to reset
        this.activeEntry.setWasModified(false);
    }

    public String getContent() { return getView().getText(); }
    public String getTitle() { return this.textField.getText(); }
    public boolean entryWasModified() {
        return this.activeEntry.isWasModified();
    }

    @Override
    public boolean requestFocusInWindow() {
        return getView().requestFocusInWindow();
    }

    private void initListeners(JTextArea textArea, JTextField textField) {
        DocumentListener contentChangeListener = createEntryModifyingListener(entry -> entry.setContent(this.getContent()));
        textArea.getDocument().addDocumentListener(contentChangeListener);

        DocumentListener titleChangeListener = createEntryModifyingListener(entry -> entry.setTitle(this.getTitle()));
        textField.getDocument().addDocumentListener(titleChangeListener);
    }

    private DocumentListener createEntryModifyingListener(Consumer<EvernoteEntry> entryConsumer) {
        return createGeneralDocumentListener(e -> {
            this.activeEntry.setWasModified(true);
            entryConsumer.accept(this.activeEntry);
        });
    }

    private JTextArea getView() { return (JTextArea) this.scrollPane.getViewport().getView(); }
}
