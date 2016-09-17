package co.kukurin.utils;

import co.kukurin.environment.Statics;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;

public class JFrameUtils {

    private static final Border sensiblePanelMargin = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    private JFrameUtils() {}

    public static void displayAndAddProperties(JFrame window,
                                               int defaultCloseOperation,
                                               String title,
                                               int minWidth,
                                               int minHeight) {
        window.getRootPane().setBorder(sensiblePanelMargin);
        window.setTitle(title);
        window.setMinimumSize(new Dimension(minWidth, minHeight));
        window.setDefaultCloseOperation(defaultCloseOperation);
        window.setLayout(new BorderLayout(5, 5));
        window.setVisible(true);
    }

}
