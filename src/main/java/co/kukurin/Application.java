package co.kukurin;

import co.kukurin.environment.ApplicationProperties;
import co.kukurin.environment.EvernoteProperties;
import co.kukurin.evernote.EvernoteAdapter;
import co.kukurin.gui.ActionFactory;
import co.kukurin.gui.JFrameUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j
public class Application extends JFrame {

    private final ApplicationProperties applicationProperties;
    private final EvernoteAdapter evernoteAdapter;

    private JTextField titleTextField;
    private JTextArea contentTextArea;
    private JButton submitNoteButton;

    public Application(EvernoteProperties evernoteProperties,
                       ApplicationProperties applicationProperties) {
        this.evernoteAdapter = new EvernoteAdapter(evernoteProperties);
        this.applicationProperties = applicationProperties;

        initWindowFromProperties();
        initGuiElements();
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
        this.submitNoteButton = new JButton(ActionFactory.createAction("Submit note", this::onSubmitNoteClick));

        setLayout(new BorderLayout(5, 5));
        add(this.titleTextField, BorderLayout.NORTH);
        add(this.contentTextArea, BorderLayout.CENTER);
        add(this.submitNoteButton, BorderLayout.SOUTH);
    }

    private void onSubmitNoteClick(ActionEvent event) {
        String noteTitle = this.titleTextField.getText();
        String noteContent = this.contentTextArea.getText();
        supplyAsync(() -> this.evernoteAdapter.storeNote(noteTitle, noteContent))
                .thenAccept(note -> log.info("stored note {}", note));
    }

}
