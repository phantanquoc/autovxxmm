/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.intellij.uiDesigner.core.GridConstraints
 *  com.intellij.uiDesigner.core.GridLayoutManager
 */
package ui.components;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReconnectingDialog
extends JDialog {
    private JPanel contentPane;

    public ReconnectingDialog() {
        this.$$$setupUI$$$();
        this.setContentPane(this.contentPane);
        this.setModal(true);
        this.setUndecorated(false);
        this.setDefaultCloseOperation(0);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    private void $$$setupUI$$$() {
        this.contentPane = new JPanel();
        this.contentPane.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        this.contentPane.add((Component)panel1, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));
        JLabel label1 = new JLabel();
        label1.setText("\u0110ang k\u1ebft n\u1ed1i");
        panel1.add((Component)label1, "Center");
    }

    public JComponent $$$getRootComponent$$$() {
        return this.contentPane;
    }
}

