package co.kukurin.utils;

import javax.swing.*;
import java.awt.*;

public class ComponentUtils {

    private ComponentUtils() {}

    public static class BorderLayoutContainerBuilder {
        final JComponent[] items;

        private BorderLayoutContainerBuilder(JComponent[] items) {
            this.items = items;
        }

        public JPanel withLayoutConstraints(String ... borderLayoutConstraints) {
            JPanel container = new JPanel(new BorderLayout());
            for(int i = 0; i < borderLayoutConstraints.length; i++) {
                container.add(items[i], borderLayoutConstraints[i]);
            }
            return container;
        }
    }

    public static BorderLayoutContainerBuilder createContainerFor(JComponent ... items) {
        return new BorderLayoutContainerBuilder(items);
    }
}
