/*
 * Decompiled with CFR 0.152.
 */
package ui.renderers;

import core.model.Bot;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import service.BotService;
import ui.ApplicationUI;

public class CollectTableCellRenderer
implements TableCellRenderer {
    private static final TableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
    private final ApplicationUI ui;

    public CollectTableCellRenderer(ApplicationUI ui) {
        this.ui = ui;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color color = Color.WHITE;
        List<Bot> botCollects = BotService.getInstance().getBotCollects();
        Bot bot = botCollects.get(row);
        if (bot.getScreen().collectScreen().isOnCollect()) {
            color = bot.getScreen().collectScreen().getBotCollect() == null ? Color.YELLOW : Color.MAGENTA;
        }
        component.setBackground(color);
        return component;
    }
}

