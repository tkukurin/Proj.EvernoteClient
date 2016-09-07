package co.kukurin.gui;

import javax.swing.*;
import java.awt.*;

public class JFrameUtils {

    private JFrameUtils() {}

    public static void displayAndAddProperties(JFrame window,
                                               int defaultCloseOperation,
                                               String title,
                                               int minWidth,
                                               int minHeight) {
        window.setTitle(title);
        window.setMinimumSize(new Dimension(minWidth, minHeight));
        window.setDefaultCloseOperation(defaultCloseOperation);
        window.setVisible(true);
    }

}
