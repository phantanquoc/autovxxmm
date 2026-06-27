/*
 * Decompiled with CFR 0.152.
 */
package ui.listeners;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import main.Application;
import ui.ApplicationUI;

public class ApplicationWindowListener
extends WindowAdapter {
    private static final int EXIT_STATUS = 0;
    private final ApplicationUI ui;

    public ApplicationWindowListener(ApplicationUI ui) {
        this.ui = ui;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (Application.confirm("B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n tho\u00e1t?")) {
            System.exit(0);
        }
    }
}

