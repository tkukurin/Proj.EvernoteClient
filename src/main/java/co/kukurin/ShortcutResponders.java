package co.kukurin;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class ShortcutResponders {

    // TODO possibly a map (keyCode -> Function)
    private final List<Function<KeyEvent, Boolean>> keyEventResponders = new LinkedList<>();
    private long previousEventTime;

    public void addKeyEvent(Predicate<KeyEvent> condition, Runnable invocation) {
        keyEventResponders.add(keyEvent -> {
            boolean shouldInvoke = condition.test(keyEvent);
            if(shouldInvoke) invocation.run();
            return shouldInvoke;
        });
    }

    public boolean eventInvoked(KeyEvent event) {
        final int requiredMinTimePassedSinceLastShortcutInvoked = 500;
        long eventTime = event.getWhen();

        if(eventTime - previousEventTime < requiredMinTimePassedSinceLastShortcutInvoked) {
            return false;
        }

        for (Function<KeyEvent, Boolean> functions : keyEventResponders) {
            if (functions.apply(event)) {
                this.previousEventTime = eventTime;
                return true;
            }
        }

        return false;
    }

}
