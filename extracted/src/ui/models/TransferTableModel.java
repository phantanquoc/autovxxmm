/*
 * Decompiled with CFR 0.152.
 */
package ui.models;

import core.model.Bot;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import service.BotService;
import ui.ApplicationUI;
import utils.Res;

public class TransferTableModel
extends DefaultTableModel {
    private static final String[] COLUMN_NAMES = new String[]{"ID", "M\u00c1Y CH\u1ee6", "T\u00c0I KHO\u1ea2N", "NH\u00c2N V\u1eacT", "TR\u1ea0NG TH\u00c1I", "V\u1eca TR\u00cd", "T\u1eccA \u0110\u1ed8", "XU"};
    private static final int[] COLUMN_WIDTHS = new int[]{25, 100, 100, 150, 180, 150, 100, 100};
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_SERVER = 1;
    private static final int COLUMN_ACCOUNT = 2;
    private static final int COLUMN_CHARACTER = 3;
    private static final int COLUMN_STATUS = 4;
    private static final int COLUMN_LOCATION = 5;
    private static final int COLUMN_POSITION = 6;
    private static final int COLUMN_COIN = 7;
    private static final long UPDATE_INTERVAL = 500L;
    private final ApplicationUI ui;
    private final TableColumnModel columnModel;
    private long lastUpdate;

    public TransferTableModel(ApplicationUI ui, TableColumnModel columnModel) {
        super(COLUMN_NAMES, 0);
        this.ui = ui;
        this.columnModel = columnModel;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void update() {
        if (Res.t() - this.lastUpdate > 500L) {
            this.setRowCount(0);
            List<Bot> botTransfer = BotService.getInstance().getBotTransfers();
            botTransfer.forEach(bot -> {
                Object[] data = new Object[COLUMN_NAMES.length];
                data[0] = bot.getId() + 1;
                data[1] = bot.getServer().getName();
                data[2] = bot.getAccount();
                data[4] = bot.getScreen().getStatusName();
                if (bot.isOnline()) {
                    data[3] = bot.getMyChar().getName() + " (lv " + bot.getMyChar().getLevel() + ")";
                    data[5] = bot.getTileMap().getMapName() + ", khu " + bot.getTileMap().getZoneId();
                    data[6] = bot.getMyChar().getPosX() + ", " + bot.getMyChar().getPosY();
                    data[7] = Res.moneyFormat(bot.getMyChar().getCoin());
                }
                this.addRow(data);
            });
            for (int i = 0; i < COLUMN_WIDTHS.length; ++i) {
                this.columnModel.getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
            }
            this.lastUpdate = Res.t();
        }
    }
}

