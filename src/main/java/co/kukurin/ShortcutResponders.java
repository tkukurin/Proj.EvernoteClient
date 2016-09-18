package co.kukurin;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
public class ShortcutResponders {

    // TODO possibly a map (keyCode -> Function)
    private final List<Function<KeyEvent, Boolean>> keyEventResponders = new LinkedList<>();

    public void addKeyReleaseEvent(String shortcut, Runnable invocation) {
        Predicate<KeyEvent> predicate = e -> true;
        for(String token : shortcut.split("\\s+")) {
            predicate = predicate.and(parseToken(token));
        }
        addKeyReleaseEvent(predicate, invocation);
    }

    public void addKeyReleaseEvent(Predicate<KeyEvent> condition, Runnable invocation) {
        keyEventResponders.add(keyEvent -> {
            boolean isReleaseEvent = keyEvent.getID() == KeyEvent.KEY_RELEASED;
            boolean shouldInvoke = condition.test(keyEvent);
            if (isReleaseEvent && shouldInvoke) invocation.run();
            return shouldInvoke;
        });
    }

    public boolean eventInvoked(KeyEvent event) {
        for (Function<KeyEvent, Boolean> functions : keyEventResponders) {
            if (functions.apply(event)) {
                return true;
            }
        }

        return false;
    }

    private Predicate<KeyEvent> parseToken(String token) {
        switch (token) {
            case "ctrl":
                return InputEvent::isControlDown;
            case "shift":
                return InputEvent::isShiftDown;
            case "alt":
                return InputEvent::isAltDown;
            case "esc":
                return e -> e.getKeyCode() == KeyEvent.VK_ESCAPE;
            case "enter":
                return e -> e.getKeyCode() == KeyEvent.VK_ENTER;
            default:
                if (token.length() != 1) throw new IllegalArgumentException("Invalid key given: " + token);
                return e -> e.getKeyCode() == token.charAt(0);
        }
    }

}
