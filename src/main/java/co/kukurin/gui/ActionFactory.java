package co.kukurin.gui;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionFactory {

    public static Action createAction(Consumer<ActionEvent> actionHandler) {
        return createAction(null, actionHandler);
    }

    public static Action createAction(String actionName,
                                      Consumer<ActionEvent> actionHandler) {
        return createAction(actionName, null, actionHandler);
    }

    public static Action createAction(String actionName,
                                      String acceleratorKey,
                                      Consumer<ActionEvent> actionHandler) {
        Action action = new AbstractAction(actionName) {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionHandler.accept(e);
            }
        };

        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey));
        //action.putValue(Action.ACTION_COMMAND_KEY, acceleratorKey);
        return action;
    }

}
