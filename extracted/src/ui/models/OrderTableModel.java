/*
 * Decompiled with CFR 0.152.
 */
package ui.models;

import core.model.Bot;
import core.model.Server;
import core.model.TileMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import service.BotService;
import ui.ApplicationUI;
import utils.Res;

public class OrderTableModel
extends DefaultTableModel {
    private static final String[] COLUMN_NAMES = new String[]{"ID", "M\u00c1Y CH\u1ee6", "T\u00c0I KHO\u1ea2N", "NH\u00c2N V\u1eacT", "TR\u1ea0NG TH\u00c1I", "GIA T\u1ed8C", "V\u1eca TR\u00cd", "TO\u1ea0 \u0110\u1ed8", "XU"};
    private static final int[] COLUMN_WIDTHS = new int[]{25, 100, 100, 150, 200, 100, 150, 80, 80};
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_SERVER = 1;
    private static final int COLUMN_ACCOUNT = 2;
    private static final int COLUMN_CHAR_NAME = 3;
    private static final int COLUMN_STATUS = 4;
    private static final int COLUMN_CLAN_NAME = 5;
    private static final int COLUMN_LOCATION = 6;
    private static final int COLUMN_POSITION = 7;
    private static final int COLUMN_COIN = 8;
    private final ApplicationUI ui;
    private final TableColumnModel columnModel;
    private int currentPage = -1;
    private final Vector<OrderRow> rows = new Vector();

    public OrderTableModel(ApplicationUI ui, TableColumnModel columnModel) {
        super(COLUMN_NAMES, 0);
        this.ui = ui;
        this.columnModel = columnModel;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void update() {
        this.updateNewValues();
        this.updateColumnsWidth();
        this.currentPage = this.ui.getCurrentPage();
        this.ui.setPageChanged(false);
        this.ui.setBotOrderChanged(false);
    }

    private void updateNewValues() {
        this.rows.removeAllElements();
        this.setRowCount(0);
        List<Bot> botOrders = BotService.getInstance().getBotOrders();
        if (!botOrders.isEmpty()) {
            int start = (this.ui.getCurrentPage() - 1) * this.ui.getPageElement();
            int stop = Math.min(start + this.ui.getPageElement(), botOrders.size());
            for (int i = start; i < stop; ++i) {
                Bot bot = botOrders.get(i);
                OrderRow row = new OrderRow(i + 1, bot);
                this.rows.add(row);
                this.addRow(row.values());
            }
        }
    }

    private void updateCurrentValues() {
        boolean hasChanged = false;
        List<Bot> botOrders = BotService.getInstance().getBotOrders();
        if (!botOrders.isEmpty()) {
            int start = (this.ui.getCurrentPage() - 1) * this.ui.getPageElement();
            int stop = Math.min(start + this.ui.getPageElement(), botOrders.size());
            for (int i = start; i < stop; ++i) {
                int orderStatus;
                Bot bot = botOrders.get(i);
                int indexRow = i - start;
                OrderRow row = this.rows.elementAt(indexRow);
                if (!row.compareServer(bot.getServer())) {
                    row.server = bot.getServer();
                    this.setValueAt(bot.getServer().getName(), indexRow, 1);
                    hasChanged = true;
                }
                if (!row.compareAccount(bot.getAccount())) {
                    row.account = bot.getAccount();
                    this.setValueAt(bot.getAccount(), indexRow, 2);
                    hasChanged = true;
                }
                if (!row.compareStatus(bot.getScreen().getStatusName())) {
                    row.status = bot.getScreen().getStatusName();
                    this.setValueAt(bot.getScreen().getStatusName(), indexRow, 4);
                    hasChanged = true;
                }
                if (!row.compareOnline(bot.isOnline())) {
                    row.update(bot);
                    this.setValueAt(row.getCharNameRow(), indexRow, 3);
                    this.setValueAt(row.getClanRow(), indexRow, 5);
                    this.setValueAt(row.getLocationRow(), indexRow, 6);
                    this.setValueAt(row.getPositionRow(), indexRow, 7);
                    this.setValueAt(row.getCoinRow(), indexRow, 8);
                    hasChanged = true;
                    continue;
                }
                if (!row.compareCharName(bot.getMyChar().getName()) || !row.compareLevel(bot.getMyChar().getLevel())) {
                    row.charName = bot.getMyChar().getName();
                    row.level = bot.getMyChar().getLevel();
                    this.setValueAt(row.getCharNameRow(), indexRow, 3);
                    hasChanged = true;
                }
                if (!row.compareClanName(bot.getMyChar().getClanName())) {
                    row.clanName = bot.getMyChar().getClanName();
                    this.setValueAt(row.getClanRow(), indexRow, 5);
                    hasChanged = true;
                }
                if (!row.compareMapId(bot.getTileMap().getMapId()) || !row.compareZoneId(bot.getTileMap().getZoneId())) {
                    row.mapId = bot.getTileMap().getMapId();
                    row.zoneId = bot.getTileMap().getZoneId();
                    this.setValueAt(row.getLocationRow(), indexRow, 6);
                    hasChanged = true;
                }
                if (!row.comparePosX(bot.getMyChar().getPosX()) || !row.comparePosY(bot.getMyChar().getPosY())) {
                    row.posX = bot.getMyChar().getPosX();
                    row.posY = bot.getMyChar().getPosY();
                    this.setValueAt(row.getPositionRow(), indexRow, 7);
                    hasChanged = true;
                }
                if (!row.compareCoin(bot.getMyChar().getCoin())) {
                    row.coin = bot.getMyChar().getCoin();
                    this.setValueAt(row.getCoinRow(), indexRow, 8);
                    hasChanged = true;
                }
                if (row.compareOrderStatus(orderStatus = bot.getOrderStatus())) continue;
                row.orderStatus = orderStatus;
                this.fireTableRowsUpdated(indexRow, indexRow);
            }
            if (hasChanged) {
                this.updateColumnsWidth();
            }
        }
    }

    private void updateColumnsWidth() {
        for (int i = 0; i < COLUMN_WIDTHS.length; ++i) {
            this.columnModel.getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }
    }

    private static class OrderRow {
        private int id;
        private boolean online;
        private Server server;
        private String account;
        private String charName;
        private String status;
        private String clanName;
        private int level;
        private int mapId;
        private int zoneId;
        private int posX;
        private int posY;
        private int coin;
        private int orderStatus = -1;

        public OrderRow(int id, Bot bot) {
            this.id = id;
            this.server = bot.getServer();
            this.account = bot.getAccount();
            this.status = bot.getScreen().getStatusName();
            this.update(bot);
        }

        public void update(Bot bot) {
            this.online = bot.isOnline();
            if (this.online) {
                this.charName = bot.getMyChar().getName();
                this.clanName = bot.getMyChar().getClanName();
                this.level = bot.getMyChar().getLevel();
                this.mapId = bot.getTileMap().getMapId();
                this.zoneId = bot.getTileMap().getZoneId();
                this.posX = bot.getMyChar().getPosX();
                this.posY = bot.getMyChar().getPosY();
                this.coin = bot.getMyChar().getCoin();
            }
        }

        public boolean compareOnline(boolean online) {
            return this.online == online;
        }

        public boolean compareServer(Server server) {
            return this.server.getId() == server.getId();
        }

        public boolean compareAccount(String account) {
            return this.account.equals(account);
        }

        public boolean compareCharName(String charName) {
            return Objects.equals(this.charName, charName);
        }

        public boolean compareStatus(String status) {
            return this.status.equals(status);
        }

        public boolean compareClanName(String clanName) {
            return Objects.equals(this.clanName, clanName);
        }

        public boolean compareLevel(int level) {
            return this.level == level;
        }

        public boolean compareMapId(int mapId) {
            return this.mapId == mapId;
        }

        public boolean compareZoneId(int zoneId) {
            return this.zoneId == zoneId;
        }

        public boolean comparePosX(int posX) {
            return this.posX == posX;
        }

        public boolean comparePosY(int posY) {
            return this.posY == posY;
        }

        public boolean compareCoin(int coin) {
            return this.coin == coin;
        }

        public boolean compareOrderStatus(int orderStatus) {
            return this.orderStatus == orderStatus;
        }

        private String getCharNameRow() {
            return this.online ? this.charName + " (lv " + this.level + ")" : null;
        }

        private String getClanRow() {
            return this.online ? (this.clanName == null || this.clanName.equals("") ? "Kh\u00f4ng" : this.clanName) : null;
        }

        private String getLocationRow() {
            return this.online ? TileMap.mapNames[this.mapId] + ", khu " + this.zoneId : null;
        }

        private String getPositionRow() {
            return this.online ? this.posX + ", " + this.posY : null;
        }

        private String getCoinRow() {
            return this.online ? Res.moneyFormat(this.coin) : null;
        }

        public Vector<Object> values() {
            Vector<Object> values = new Vector<Object>();
            values.add(this.id);
            values.add(this.server.getName());
            values.add(this.account);
            values.add(this.getCharNameRow());
            values.add(this.status);
            values.add(this.getClanRow());
            values.add(this.getLocationRow());
            values.add(this.getPositionRow());
            values.add(this.getCoinRow());
            return values;
        }
    }
}

