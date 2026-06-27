/*
 * Decompiled with CFR 0.152.
 */
package ui;

import core.model.Bot;
import core.model.Char;
import core.model.TileMap;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import utils.Res;

@Deprecated
public class ViewPlayerScreen
extends JFrame {
    private JPanel contentPane;
    private Bot account;
    private JTable listPlayer;
    private long lastUpdate;
    private JLabel lbNumPlayer;

    public ViewPlayerScreen() {
        this.setDefaultCloseOperation(2);
        this.setBounds(100, 100, 450, 300);
        this.contentPane = new JPanel();
        this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setContentPane(this.contentPane);
        JScrollPane scrollPane = new JScrollPane();
        JLabel lb_NumPlayer = new JLabel("S\u1ed1 Ng\u01b0\u1eddi Trong Khu:");
        this.lbNumPlayer = new JLabel(" ");
        GroupLayout gl_contentPane = new GroupLayout(this.contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(gl_contentPane.createSequentialGroup().addComponent(lb_NumPlayer).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.lbNumPlayer, -1, 318, Short.MAX_VALUE)).addComponent(scrollPane, -1, 426, Short.MAX_VALUE));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(gl_contentPane.createSequentialGroup().addGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lb_NumPlayer).addComponent(this.lbNumPlayer)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(scrollPane, -1, 233, Short.MAX_VALUE)));
        this.listPlayer = new JTable(){

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.listPlayer.setCellSelectionEnabled(false);
        this.listPlayer.setRowSelectionAllowed(true);
        scrollPane.setViewportView(this.listPlayer);
        this.contentPane.setLayout(gl_contentPane);
        this.setAlwaysOnTop(true);
        this.setLocationRelativeTo(null);
    }

    public void show(Bot account) {
        this.account = account;
        this.setVisible(true);
    }

    public void update() {
        this.setTitle("Danh s\u00e1ch ng\u01b0\u1eddi ch\u01a1i t\u1ea1i: " + TileMap.mapNames[this.account.getTileMap().getMapId()] + ", khu " + this.account.getTileMap().getZoneId());
        this.lbNumPlayer.setText(String.valueOf(this.account.getCharsInMap().size()));
        if (this.account.getCharsInMap().size() == 0) {
            this.listPlayer.setModel(new DefaultTableModel((Object[][])null, new String[]{"NH\u00c2N V\u1eacT", "LEVEL", "TO\u1ea0 \u0110\u1ed8", "HP/HP MAX"}));
            return;
        }
        Object[][] data = new String[this.account.getCharsInMap().size()][4];
        for (int i = 0; i < this.account.getCharsInMap().size(); ++i) {
            Char c = (Char)this.account.getCharsInMap().elementAt(i);
            if (c == null) continue;
            data[i][0] = c.getName();
            data[i][1] = String.valueOf(c.getLevel());
            data[i][2] = c.getPosX() + ":" + c.getPosY();
            data[i][3] = Res.moneyFormat(c.getHp()) + "/" + Res.moneyFormat(c.getMaxHP());
        }
        this.listPlayer.setModel(new DefaultTableModel(data, new String[]{"NH\u00c2N V\u1eacT", "LEVEL", "TO\u1ea0 \u0110\u1ed8", "HP/HP MAX"}));
    }
}

