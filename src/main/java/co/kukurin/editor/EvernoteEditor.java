package co.kukurin.editor;

import co.kukurin.custom.Optional;
import co.kukurin.environment.Statics;
import co.kukurin.evernote.EvernoteEntry;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

import static co.kukurin.gui.factories.DocumentListenerFactory.*;
import static java.awt.BorderLayout.*;

public class EvernoteEditor extends JPanel {
    // TODO move this somewhere in properties.
    private static final Font editorFont = new Font("Consolas", Font.PLAIN, 14);

    private EvernoteEntry activeEntry;
    private final JScrollPane scrollPane;
    private final JTextField textField;

    public EvernoteEditor() {
        JTextArea textArea = new JTextArea();

        this.scrollPane = new JScrollPane(textArea);
        this.textField = new JTextField();

        textArea.setFont(editorFont);
        this.textField.setFont(editorFont);

        setLayout(new BorderLayout(0, Statics.defaultMargin));
        add(this.textField, PAGE_START);
        add(this.scrollPane, CENTER);

        initListeners(textArea, this.textField);
    }

    public void setEntry(EvernoteEntry entry) {
        this.activeEntry = entry;
        this.textField.setText(entry.getTitle());
        getView().setText(entry.getContent());
    }

    public String getContent() { return getView().getText(); }
    public String getTitle() { return this.textField.getText(); }

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
        return createGeneralDocumentListener(e ->
                Optional.ofNullable(this.activeEntry).ifPresent(entry -> {
                    entry.setWasModified(true);
                    entryConsumer.accept(entry);
                })
        );
    }

    private JTextArea getView() { return (JTextArea) this.scrollPane.getViewport().getView(); }
}
