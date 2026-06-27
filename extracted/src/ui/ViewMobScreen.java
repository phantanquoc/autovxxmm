/*
 * Decompiled with CFR 0.152.
 */
package ui;

import core.model.Bot;
import core.model.Mob;
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
public class ViewMobScreen
extends JFrame {
    private JPanel contentPane;
    private Bot account;
    private JTable listMob;
    private long lastUpdate;
    private JLabel lbNumMob;

    public ViewMobScreen() {
        this.setDefaultCloseOperation(2);
        this.setBounds(100, 100, 450, 300);
        this.contentPane = new JPanel();
        this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setContentPane(this.contentPane);
        JScrollPane scrollPane = new JScrollPane();
        JLabel lb_NumMobMap = new JLabel("S\u1ed1 qu\u00e1i trong map:");
        this.lbNumMob = new JLabel(" ");
        GroupLayout gl_contentPane = new GroupLayout(this.contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(scrollPane, -1, 426, Short.MAX_VALUE).addGroup(gl_contentPane.createSequentialGroup().addComponent(lb_NumMobMap).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.lbNumMob, -1, 323, Short.MAX_VALUE)));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(gl_contentPane.createSequentialGroup().addGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lb_NumMobMap).addComponent(this.lbNumMob)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(scrollPane, -1, 233, Short.MAX_VALUE)));
        this.listMob = new JTable(){

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.listMob.setCellSelectionEnabled(false);
        this.listMob.setRowSelectionAllowed(true);
        scrollPane.setViewportView(this.listMob);
        this.contentPane.setLayout(gl_contentPane);
        this.setAlwaysOnTop(true);
        this.setLocationRelativeTo(null);
    }

    public void show(Bot account) {
        this.account = account;
        this.setVisible(true);
    }

    public void update() {
        this.setTitle("Danh s\u00e1ch qu\u00e1i t\u1ea1i: " + TileMap.mapNames[this.account.getTileMap().getMapId()] + ", khu " + this.account.getTileMap().getZoneId());
        this.lbNumMob.setText(String.valueOf(this.account.getMobs().size()));
        if (this.account.getMobs().size() == 0) {
            this.listMob.setModel(new DefaultTableModel((Object[][])null, new String[]{"T\u00caN", "ID", "LEVEL", "TO\u1ea0 \u0110\u1ed8", "HP/HP MAX", "BOSS", "ACTIVE", "STATUS"}));
            return;
        }
        Object[][] data = new String[this.account.getMobs().size()][8];
        for (int i = 0; i < this.account.getMobs().size(); ++i) {
            Mob m = (Mob)this.account.getMobs().elementAt(i);
            if (m == null) continue;
            data[i][0] = m.getTemplate().name;
            data[i][1] = String.valueOf(m.templateId);
            data[i][2] = String.valueOf(m.level);
            data[i][3] = m.x + ":" + m.y;
            data[i][4] = Res.moneyFormat(m.hp) + "/" + Res.moneyFormat(m.maxHp);
            data[i][5] = m.isBoss ? "TRUE" : "FALSE";
            data[i][6] = m.status != 0 ? "TRUE" : "FALSE";
            data[i][7] = String.valueOf(m.status);
        }
        this.listMob.setModel(new DefaultTableModel(data, new String[]{"T\u00caN", "ID", "LEVEL", "TO\u1ea0 \u0110\u1ed8", "HP/HP MAX", "BOSS", "ACTIVE", "STATUS"}));
    }
}

