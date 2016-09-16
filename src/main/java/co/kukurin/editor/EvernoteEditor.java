package co.kukurin.editor;

import co.kukurin.custom.Optional;
import co.kukurin.environment.Statics;
import co.kukurin.evernote.EvernoteEntry;
import co.kukurin.gui.factories.DocumentListenerFactory;

import javax.swing.*;
import java.awt.*;

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
        textArea.getDocument().addDocumentListener(DocumentListenerFactory.createGeneralDocumentListener(e ->
                Optional.ofNullable(this.activeEntry).ifPresent(entry -> {
                    entry.setWasModified(true);
                    entry.setContent(this.getContent());
                })
        ));

        textField.getDocument().addDocumentListener(DocumentListenerFactory.createGeneralDocumentListener(e ->
                Optional.ofNullable(this.activeEntry).ifPresent(entry -> {
                    entry.setWasModified(true);
                    entry.setTitle(this.getTitle());
                })
        ));
    }

    private JTextArea getView() { return (JTextArea) this.scrollPane.getViewport().getView(); }
}
