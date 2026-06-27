/*
 * Decompiled with CFR 0.152.
 */
package ui.listeners;

import core.model.Bot;
import core.model.TileMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import main.Application;
import service.BotService;
import ui.ApplicationUI;
import utils.Res;

public class ApplicationUIActionListener
implements ActionListener {
    private ApplicationUI ui;
    private long timeUpdate = 0L;
    private int tick;
    private String loading = "";

    public ApplicationUIActionListener(ApplicationUI ui) {
        this.ui = ui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (this.ui.isVisible() && this.isUpdate()) {
                this.ui.getOrderTableModel().update();
                this.ui.getTransferTableModel().update();
                this.ui.getCollectTableModel().update();
                this.updateApplicationInformation();
                this.updateSelectedInformation();
                this.setTimeUpdate();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isUpdate() {
        return Res.t() > this.timeUpdate;
    }

    private void updateApplicationInformation() {
        if (this.tick++ > 3) {
            this.tick = 0;
            this.loading = this.loading.length() < 3 ? this.loading + "." : "";
        }
        String operatingTime = Application.systemInterrupt ? "\u0110ang k\u1ebft n\u1ed1i" + this.loading : Res.toTime(Res.calculateSecond(this.ui.getTimeStart()));
        this.ui.getLabel_OperatingTime().setText(operatingTime);
        this.ui.getLabel_ActiveBot().setText(Res.addZero(BotService.getInstance().getNumActiveOrderBot()) + "/" + Res.addZero(BotService.getInstance().getBotOrders().size()));
    }

    private void updateSelectedInformation() {
        Bot bot = this.ui.getSelectedBotOrder();
        boolean setEmptyLabel = false;
        if (bot != null) {
            this.ui.getLabel_Account().setText(bot.getAccount());
            if (bot.isOnline()) {
                this.ui.getLabel_Character().setText(bot.getMyChar().getName());
                this.ui.getLabel_Map().setText(TileMap.mapNames[bot.getTileMap().getMapId()]);
                this.ui.getLabel_Zone().setText(String.valueOf(bot.getTileMap().getZoneId()));
                this.ui.getLabel_X().setText(String.valueOf(bot.getMyChar().getPosX()));
                this.ui.getLabel_Y().setText(String.valueOf(bot.getMyChar().getPosY()));
                this.ui.getLabel_Kins().setText(Res.moneyFormat(bot.getMyChar().getKins()));
                this.ui.getLabel_Coins().setText(Res.moneyFormat(bot.getMyChar().getCoin()));
                this.ui.getLabel_Golds().setText(Res.moneyFormat(bot.getMyChar().getGold()));
            } else {
                setEmptyLabel = true;
            }
        } else {
            this.ui.getLabel_Account().setText("");
            setEmptyLabel = true;
        }
        if (setEmptyLabel) {
            this.ui.getLabel_Character().setText("");
            this.ui.getLabel_Map().setText("");
            this.ui.getLabel_Zone().setText("");
            this.ui.getLabel_X().setText("");
            this.ui.getLabel_Y().setText("");
            this.ui.getLabel_Kins().setText("");
            this.ui.getLabel_Coins().setText("");
            this.ui.getLabel_Golds().setText("");
        }
    }

    public void setTimeUpdate() {
        this.setTimeUpdate(100L);
    }

    public void setTimeUpdate(long milliseconds) {
        this.timeUpdate = Res.t() + milliseconds;
    }
}

