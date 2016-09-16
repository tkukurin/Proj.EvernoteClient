package co.kukurin.gui;

import co.kukurin.environment.Statics;

import javax.swing.*;

public class ComponentUtils {

    private ComponentUtils() {}

    public static class BorderLayoutContainerBuilder {
        final JComponent[] items;

        private BorderLayoutContainerBuilder(JComponent[] items) {
            this.items = items;
        }

        public JPanel withLayoutConstraints(String ... borderLayoutConstraints) {
            JPanel container = new JPanel(Statics.defaultBorderLayout);
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
