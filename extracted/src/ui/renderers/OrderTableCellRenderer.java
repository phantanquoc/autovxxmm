/*
 * Decompiled with CFR 0.152.
 */
package ui.renderers;

import core.model.Bot;
import core.model.Order;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import service.BotService;
import ui.ApplicationUI;

public class OrderTableCellRenderer
implements TableCellRenderer {
    private static final TableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
    private ApplicationUI ui;

    public OrderTableCellRenderer(ApplicationUI ui) {
        this.ui = ui;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color color = Color.WHITE;
        try {
            if (row == this.ui.getOrderTableSelectedRow()) {
                color = Color.CYAN;
            } else {
                Bot bot = BotService.getInstance().getBotOrders().get(row);
                if (bot.isOnline()) {
                    Order order = bot.getScreen().orderScreen().getOrder();
                    if (order != null) {
                        if (order.hasStatus(0)) {
                            color = Color.YELLOW;
                        } else if (order.hasStatus(1)) {
                            color = Color.BLUE;
                        } else if (order.hasStatus(3)) {
                            color = Color.RED;
                        }
                    }
                } else if (bot.getAutoLogin().isLoginSubmiting) {
                    color = Color.MAGENTA;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        component.setBackground(color);
        return component;
    }
}

