package co.kukurin.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class KeyAdapterFactory {

    private KeyAdapterFactory() {}

    public static KeyAdapter createOnReleaseEvent(Consumer<KeyEvent> keyEventConsumer) {
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                keyEventConsumer.accept(e);
            }
        };
    }

    public static KeyAdapter createOnPressEvent(Consumer<KeyEvent> keyEventConsumer) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyEventConsumer.accept(e);
            }
        };
    }

    public static KeyAdapter createOnTypedEvent(Consumer<KeyEvent> keyEventConsumer) {
        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                keyEventConsumer.accept(e);
            }
        };
    }

}
