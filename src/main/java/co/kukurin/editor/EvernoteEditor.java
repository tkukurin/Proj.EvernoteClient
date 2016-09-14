package co.kukurin.editor;

import co.kukurin.environment.Statics;

import javax.swing.*;
import java.awt.*;

public class EvernoteEditor extends JPanel implements ModifiableContentHolder {

    // TODO move this somewhere in properties.
    private static final Font editorFont = new Font("Consolas", Font.PLAIN, 14);
    private final JScrollPane scrollPane;

    public EvernoteEditor() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(editorFont);

        this.scrollPane = new JScrollPane(textArea);

        setLayout(Statics.defaultBorderLayout);
        add(this.scrollPane, BorderLayout.CENTER);
    }

    public void setText(String text) {
        getView().setText(text);
    }
    public String getText() { return getView().getText(); }

    @Override
    public boolean requestFocusInWindow() {
        return getView().requestFocusInWindow();
    }

    @Override
    public boolean hasModifiedContent() {
        return false;
    }

    private JTextArea getView() { return (JTextArea) this.scrollPane.getViewport().getView(); }
}
