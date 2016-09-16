package co.kukurin.gui.factories;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class DocumentListenerFactory {
    private DocumentListenerFactory() {}

    public static DocumentListener createGeneralDocumentListener(Consumer<DocumentEvent> anyDocumentEventConsumer) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                anyDocumentEventConsumer.accept(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                anyDocumentEventConsumer.accept(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                anyDocumentEventConsumer.accept(e);
            }
        };
    }
}
