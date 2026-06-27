/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.intellij.uiDesigner.core.GridConstraints
 *  com.intellij.uiDesigner.core.GridLayoutManager
 */
package ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import constants.Strings;
import core.model.Server;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import main.Application;
import service.BlockService;
import service.ServerService;
import service.entity.Blocker;

public class BlockUI
extends JFrame {
    private static final String[] COLUMN_NAMES = new String[]{"ID", "M\u00e1y ch\u1ee7", "T\u00ean"};
    private static final BlockUI instance = new BlockUI();
    private JButton button_Delete;
    private JButton button_Close;
    private JTextField field_Name;
    private JButton button_Add;
    private JTable table_Content;
    private DefaultTableModel tableModel;
    private JComboBox<Server> combobox_SelectServer;
    private JPanel panel_Content;

    public BlockUI() {
        this.$$$setupUI$$$();
        this.setContentPane(this.panel_Content);
        this.setDefaultCloseOperation(2);
        this.setTitle(Strings.BLOCK_TITLE);
        this.setSize(500, 380);
        this.setLocationRelativeTo(null);
        for (Server server : ServerService.getInstance().getServers()) {
            this.combobox_SelectServer.addItem(server);
        }
        this.combobox_SelectServer.setSelectedIndex(0);
        this.table_Content.setCellSelectionEnabled(false);
        this.table_Content.setRowSelectionAllowed(true);
        this.table_Content.getTableHeader().setReorderingAllowed(false);
        this.table_Content.setSelectionBackground(Color.CYAN);
        this.button_Add.addActionListener(e -> {
            Server server = (Server)this.combobox_SelectServer.getSelectedItem();
            String name = this.field_Name.getText().trim();
            if (server == null) {
                Application.alert(this, "Ch\u01b0a ch\u1ecdn m\u00e1y ch\u1ee7!");
                return;
            }
            if (name.isEmpty()) {
                Application.alert(this, "T\u00ean kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!");
                return;
            }
            if (BlockService.getInstance().isContains(server.getId(), name)) {
                Application.alert(this, "T\u00ean \u0111\u00e3 t\u1ed3n t\u1ea1i!");
                return;
            }
            Blocker blocker = new Blocker(server.getId(), name);
            BlockService.getInstance().add(blocker);
            this.field_Name.setText("");
            this.updateTable();
        });
        this.button_Delete.addActionListener(e -> {
            int row = this.table_Content.getSelectedRow();
            if (row == -1) {
                Application.alert(this, "H\u00e3y ch\u1ecdn h\u00e0ng tr\u01b0\u1edbc khi xo\u00e1!");
                return;
            }
            BlockService.getInstance().remove(row);
            this.updateTable();
        });
        this.button_Close.addActionListener(e -> this.close());
    }

    public void open() {
        this.updateTable();
        this.setVisible(true);
    }

    private void updateTable() {
        List<Blocker> blockers = BlockService.getInstance().getBlockers();
        Object[][] data = new Object[blockers.size()][3];
        for (int i = 0; i < blockers.size(); ++i) {
            Blocker blocker = blockers.get(i);
            data[i] = new Object[]{i + 1, ServerService.getInstance().getServerName(blocker.getServerId()), blocker.getName()};
        }
        this.tableModel = new DefaultTableModel(data, COLUMN_NAMES){

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table_Content.setModel(this.tableModel);
    }

    public void close() {
        this.setVisible(false);
    }

    private void $$$setupUI$$$() {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        this.panel_Content = new JPanel();
        this.panel_Content.setLayout((LayoutManager)new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        panel1.add((Component)this.panel_Content, "Center");
        JPanel panel2 = new JPanel();
        panel2.setLayout((LayoutManager)new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_Content.add((Component)panel2, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 0, null, null, null, 0, false));
        this.button_Delete = new JButton();
        this.button_Delete.setText("Xo\u00e1");
        panel2.add((Component)this.button_Delete, new GridConstraints(0, 3, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), null, 0, false));
        this.button_Close = new JButton();
        this.button_Close.setText("\u0110\u00f3ng");
        panel2.add((Component)this.button_Close, new GridConstraints(0, 4, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), null, 0, false));
        this.field_Name = new JTextField();
        panel2.add((Component)this.field_Name, new GridConstraints(0, 0, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        this.button_Add = new JButton();
        this.button_Add.setText("Th\u00eam");
        panel2.add((Component)this.button_Add, new GridConstraints(0, 2, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), null, 0, false));
        this.combobox_SelectServer = new JComboBox();
        panel2.add(this.combobox_SelectServer, new GridConstraints(0, 1, 1, 1, 8, 1, 2, 0, new Dimension(-1, 22), new Dimension(-1, 22), null, 0, false));
        JScrollPane scrollPane1 = new JScrollPane();
        this.panel_Content.add((Component)scrollPane1, new GridConstraints(0, 0, 1, 1, 0, 3, 5, 5, new Dimension(400, 280), new Dimension(400, 280), null, 0, false));
        this.table_Content = new JTable();
        scrollPane1.setViewportView(this.table_Content);
    }

    public static BlockUI getInstance() {
        return instance;
    }
}

