package co.kukurin.gui;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class PredefinedKeyEvents {

    private final List<Function<KeyEvent, Boolean>> keyEventResponders = new LinkedList<>();

    public void addKeyEvent(Predicate<KeyEvent> condition, Runnable invocation) {
        keyEventResponders.add(keyEvent -> {
            boolean shouldInvoke = condition.test(keyEvent);
            if(shouldInvoke) invocation.run();
            return shouldInvoke;
        });
    }

    public boolean eventInvoked(KeyEvent event) {
        for(Function<KeyEvent, Boolean> functions : keyEventResponders) {
            if(functions.apply(event))
                return true;
        }

        return false;
    }

}
