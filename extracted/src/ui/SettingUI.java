/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.intellij.uiDesigner.core.GridConstraints
 *  com.intellij.uiDesigner.core.GridLayoutManager
 *  com.intellij.uiDesigner.core.Spacer
 *  com.jgoodies.forms.layout.CellConstraints
 *  com.jgoodies.forms.layout.FormLayout
 */
package ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import constants.Strings;
import core.service.NextMap;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import main.Application;
import service.SettingService;
import utils.Res;

public class SettingUI
extends JFrame {
    private static final SettingUI instance = new SettingUI();
    private JPanel panel_Content;
    private JTextField field_LoginLimit;
    private JTextField field_ProtectionCode;
    private JCheckBox checkbox_AutoRegisterProtection;
    private JCheckBox checkbox_AutoOpenProtection;
    private JCheckBox checkbox_ProtectBot;
    private JButton button_Save;
    private JButton button_Close;
    private JTextField field_TimeCreateOrder;
    private JTextField field_TimeBlockSpam;
    private JTextField field_BlockSpamAfter;
    private JTextField field_MessageOrder;
    private JCheckBox checkbox_EnableMessageOrder;
    private JCheckBox checkbox_EnableMessageBet;
    private JCheckBox checkbox_EnableMessageLose;
    private JCheckBox checkbox_EnableMessageWin;
    private JCheckBox checkbox_EnableMessageReward;
    private JTextField field_MessageBet;
    private JTextField field_MessageLose;
    private JTextField field_MessageWin;
    private JTextField field_MessageReward;
    private JCheckBox checkbox_EnableSaveReturnPoint;
    private JTextField field_MapSaveReturnPoint;
    private JCheckBox checkbox_EnableMessageOrderError;
    private JTextField field_MessageOrderError;
    private JCheckBox checkbox_EnableKeepBotOnline;
    private JTextField field_MinuteKeepBotOnline;
    private JTextField field_LoginStagger;
    private JTextField field_LoginInterval;
    private boolean isUpdating = false;

    public SettingUI() {
        this.$$$setupUI$$$();
        this.setContentPane(this.panel_Content);
        this.setDefaultCloseOperation(2);
        this.setTitle(Strings.SETTING_TITLE);
        this.setSize(450, 700);
        this.setLocationRelativeTo(null);
        this.button_Close.addActionListener(e -> this.setVisible(false));
        this.button_Save.addActionListener(e -> {
            int loginLimit = Res.getNumber(this.field_LoginLimit.getText().trim());
            String protectionCode = this.field_ProtectionCode.getText();
            boolean autoRegisterProtection = this.checkbox_AutoRegisterProtection.isSelected();
            boolean autoOpenProtection = this.checkbox_AutoOpenProtection.isSelected();
            boolean protectBot = this.checkbox_ProtectBot.isSelected();
            int timeCreateOrder = Res.getNumber(this.field_TimeCreateOrder.getText().trim());
            int timeBlockSpam = Res.getNumber(this.field_TimeBlockSpam.getText().trim());
            int blockSpamAfter = Res.getNumber(this.field_BlockSpamAfter.getText().trim());
            boolean enableMessageOrder = this.checkbox_EnableMessageOrder.isSelected();
            String messageOrder = this.field_MessageOrder.getText();
            boolean enableMessageBet = this.checkbox_EnableMessageBet.isSelected();
            String messageBet = this.field_MessageBet.getText();
            boolean enableMessageLose = this.checkbox_EnableMessageLose.isSelected();
            String messageLose = this.field_MessageLose.getText();
            boolean enableMessageWin = this.checkbox_EnableMessageWin.isSelected();
            String messageWin = this.field_MessageWin.getText();
            boolean enableMessageReward = this.checkbox_EnableMessageReward.isSelected();
            String messageReward = this.field_MessageReward.getText();
            boolean enableMessageOrderError = this.checkbox_EnableMessageOrderError.isSelected();
            String messageOrderError = this.field_MessageOrderError.getText();
            boolean saveReturnPoint = this.checkbox_EnableSaveReturnPoint.isSelected();
            int mapSaveReturnPoint = Res.getNumber(this.field_MapSaveReturnPoint.getText().trim());
            boolean enableKeepBotOnline = this.checkbox_EnableKeepBotOnline.isSelected();
            int minuteKeepBotOnline = Res.getNumber(this.field_MinuteKeepBotOnline.getText().trim());
            int loginStagger = Res.getNumber(this.field_LoginStagger.getText().trim());
            int loginInterval = Res.getNumber(this.field_LoginInterval.getText().trim());
            String cause = null;
            if (!SettingService.isValidProtectionCode(protectionCode) && autoRegisterProtection) {
                cause = "M\u00e3 b\u1ea3o v\u1ec7 ph\u1ea3i g\u1ed3m 6 ch\u1eef s\u1ed1 v\u00e0 kh\u00f4ng ch\u1ee9a k\u00ed t\u1ef1 \u0111\u1eb7c bi\u1ec7t!";
            } else if (loginLimit == -1) {
                cause = "Thi\u1ebft l\u1eadp gi\u1edbi h\u1ea1n \u0111\u0103ng nh\u1eadp kh\u00f4ng h\u1ee3p l\u1ec7!";
            } else if (timeCreateOrder == -1) {
                cause = "Thi\u1ebft l\u1eadp th\u1eddi gian t\u1ea1o \u0111\u01a1n kh\u00f4ng h\u1ee3p l\u1ec7!";
            } else if (timeBlockSpam == -1) {
                cause = "Thi\u1ebft l\u1eadp th\u1eddi gian ch\u1eb7n spam kh\u00f4ng h\u1ee3p l\u1ec7!";
            } else if (blockSpamAfter == -1) {
                cause = "Thi\u1ebft l\u1eadp ch\u1eb7n spam kh\u00f4ng h\u1ee3p l\u1ec7!";
            } else if (!NextMap.isSchool(mapSaveReturnPoint) && !NextMap.isVillage(mapSaveReturnPoint)) {
                cause = "Map l\u01b0u to\u1ea1 \u0111\u1ed9 ph\u1ea3i \u1edf tr\u01b0\u1eddng ho\u1eb7c l\u00e0ng!";
            } else if (enableKeepBotOnline && minuteKeepBotOnline < 1) {
                cause = "S\u1ed1 ph\u00fat gi\u1eef bot online ph\u1ea3i l\u1edbn h\u01a1n 0!";
            } else if (loginStagger == -1) {
                cause = "Gi\u00e3n c\u00e1ch login kh\u00f4ng h\u1ee3p l\u1ec7!";
            } else if (loginInterval == -1) {
                cause = "Nh\u1ecbp login t\u1ed1i thi\u1ec3u kh\u00f4ng h\u1ee3p l\u1ec7!";
            }
            if (cause != null) {
                Application.error(this, cause);
                return;
            }
            SettingService setting = SettingService.getInstance();
            setting.setLoginLimit(loginLimit);
            setting.setProtectionCode(protectionCode);
            setting.setAutoRegisterProtection(autoRegisterProtection);
            setting.setAutoOpenProtection(autoOpenProtection);
            setting.setProtectBot(protectBot);
            setting.setTimeCreateOrder(timeCreateOrder);
            setting.setTimeBlockSpam(timeBlockSpam);
            setting.setBlockSpamAfter(blockSpamAfter);
            setting.setEnableMessageOrder(enableMessageOrder);
            setting.setMessageOrder(messageOrder);
            setting.setEnableMessageBet(enableMessageBet);
            setting.setMessageBet(messageBet);
            setting.setEnableMessageLose(enableMessageLose);
            setting.setMessageLose(messageLose);
            setting.setEnableMessageWin(enableMessageWin);
            setting.setMessageWin(messageWin);
            setting.setEnableMessageReward(enableMessageReward);
            setting.setMessageReward(messageReward);
            setting.setSaveReturnPoint(saveReturnPoint);
            setting.setMapSaveReturnPoint(mapSaveReturnPoint);
            setting.setEnableMessageOrderError(enableMessageOrderError);
            setting.setMessageOrderError(messageOrderError);
            setting.setEnableKeepBotOnline(enableKeepBotOnline);
            if (enableKeepBotOnline) {
                setting.setMinuteKeepBotOnline(minuteKeepBotOnline);
            }
            setting.setLoginStagger(loginStagger);
            setting.setLoginInterval(loginInterval);
            setting.save();
            this.dispose();
            Application.info(this, "L\u01b0u c\u00e0i \u0111\u1eb7t th\u00e0nh c\u00f4ng!");
        });
        this.checkbox_EnableMessageOrder.addActionListener(e -> this.updateEnableState());
        this.checkbox_EnableMessageBet.addActionListener(e -> this.updateEnableState());
        this.checkbox_EnableMessageLose.addActionListener(e -> this.updateEnableState());
        this.checkbox_EnableMessageWin.addActionListener(e -> this.updateEnableState());
        this.checkbox_EnableMessageReward.addActionListener(e -> this.updateEnableState());
        this.checkbox_EnableMessageOrderError.addActionListener(e -> this.updateEnableState());
        this.checkbox_EnableSaveReturnPoint.addActionListener(e -> this.updateEnableState());
        this.checkbox_EnableKeepBotOnline.addActionListener(e -> this.updateEnableState());
    }

    public void open() {
        SettingService setting = SettingService.getInstance();
        this.field_LoginLimit.setText(String.valueOf(setting.getLoginLimit()));
        this.field_ProtectionCode.setText(setting.getProtectionCode());
        this.checkbox_AutoRegisterProtection.setSelected(setting.isAutoRegisterProtection());
        this.checkbox_AutoOpenProtection.setSelected(setting.isAutoOpenProtection());
        this.checkbox_ProtectBot.setSelected(setting.isProtectBot());
        this.field_TimeCreateOrder.setText(String.valueOf(setting.getTimeCreateOrder()));
        this.field_TimeBlockSpam.setText(String.valueOf(setting.getTimeBlockSpam()));
        this.field_BlockSpamAfter.setText(String.valueOf(setting.getBlockSpamAfter()));
        this.checkbox_EnableMessageOrder.setSelected(setting.isEnableMessageOrder());
        this.checkbox_EnableMessageBet.setSelected(setting.isEnableMessageBet());
        this.checkbox_EnableMessageLose.setSelected(setting.isEnableMessageLose());
        this.checkbox_EnableMessageWin.setSelected(setting.isEnableMessageWin());
        this.checkbox_EnableMessageReward.setSelected(setting.isEnableMessageReward());
        this.checkbox_EnableMessageOrderError.setSelected(setting.isEnableMessageOrderError());
        this.field_MessageOrder.setText(setting.getMessageOrder());
        this.field_MessageBet.setText(setting.getMessageBet());
        this.field_MessageLose.setText(setting.getMessageLose());
        this.field_MessageWin.setText(setting.getMessageWin());
        this.field_MessageReward.setText(setting.getMessageReward());
        this.field_MessageOrderError.setText(setting.getMessageOrderError());
        this.checkbox_EnableSaveReturnPoint.setSelected(setting.isSaveReturnPoint());
        this.field_MapSaveReturnPoint.setText(String.valueOf(setting.getMapSaveReturnPoint()));
        this.checkbox_EnableKeepBotOnline.setSelected(setting.isEnableKeepBotOnline());
        this.field_MinuteKeepBotOnline.setText(String.valueOf(setting.getMinuteKeepBotOnline()));
        this.field_LoginStagger.setText(String.valueOf(setting.getLoginStagger()));
        this.field_LoginInterval.setText(String.valueOf(setting.getLoginInterval()));
        this.updateEnableState();
        this.setVisible(true);
    }

    private void updateEnableState() {
        this.field_MessageOrder.setEnabled(this.checkbox_EnableMessageOrder.isSelected());
        this.field_MessageBet.setEnabled(this.checkbox_EnableMessageBet.isSelected());
        this.field_MessageLose.setEnabled(this.checkbox_EnableMessageLose.isSelected());
        this.field_MessageWin.setEnabled(this.checkbox_EnableMessageWin.isSelected());
        this.field_MessageReward.setEnabled(this.checkbox_EnableMessageReward.isSelected());
        this.field_MessageOrderError.setEnabled(this.checkbox_EnableMessageOrderError.isSelected());
        this.field_MapSaveReturnPoint.setEnabled(this.checkbox_EnableSaveReturnPoint.isSelected());
        this.field_MinuteKeepBotOnline.setEnabled(this.checkbox_EnableKeepBotOnline.isSelected());
    }

    private void $$$setupUI$$$() {
        this.panel_Content = new JPanel();
        this.panel_Content.setLayout((LayoutManager)new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        this.panel_Content.add((Component)panel1, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));
        JPanel panel2 = new JPanel();
        panel2.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add((Component)panel2, "Center");
        JPanel panel3 = new JPanel();
        panel3.setLayout((LayoutManager)new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        panel2.add((Component)panel3, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, new Dimension(280, 400), null, 0, false));
        JLabel label1 = new JLabel();
        label1.setText("Gi\u1edbi h\u1ea1n \u0111\u0103ng nh\u1eadp:");
        CellConstraints cc = new CellConstraints();
        panel3.add((Component)label1, cc.xy(1, 1));
        JLabel label2 = new JLabel();
        label2.setText("M\u1eadt kh\u1ea9u r\u01b0\u01a1ng:");
        panel3.add((Component)label2, cc.xy(1, 3));
        JLabel label3 = new JLabel();
        label3.setText("T\u1ef1 \u0111\u0103ng k\u00ed b\u1ea3o v\u1ec7 r\u01b0\u01a1ng:");
        panel3.add((Component)label3, cc.xy(1, 5));
        JLabel label4 = new JLabel();
        label4.setText("T\u1ef1 m\u1edf b\u1ea3o v\u1ec7 r\u01b0\u01a1ng:");
        panel3.add((Component)label4, cc.xy(1, 7));
        JLabel label5 = new JLabel();
        label5.setText("B\u1ea3o v\u1ec7 bot:");
        panel3.add((Component)label5, cc.xy(1, 9));
        this.field_LoginLimit = new JTextField();
        panel3.add((Component)this.field_LoginLimit, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        this.field_ProtectionCode = new JTextField();
        panel3.add((Component)this.field_ProtectionCode, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        this.checkbox_AutoRegisterProtection = new JCheckBox();
        this.checkbox_AutoRegisterProtection.setText("B\u1eadt/T\u1eaft");
        panel3.add((Component)this.checkbox_AutoRegisterProtection, cc.xy(3, 5));
        this.checkbox_AutoOpenProtection = new JCheckBox();
        this.checkbox_AutoOpenProtection.setText("B\u1eadt/T\u1eaft");
        panel3.add((Component)this.checkbox_AutoOpenProtection, cc.xy(3, 7));
        this.checkbox_ProtectBot = new JCheckBox();
        this.checkbox_ProtectBot.setText("B\u1eadt/T\u1eaft");
        panel3.add((Component)this.checkbox_ProtectBot, cc.xy(3, 9));
        JLabel label6 = new JLabel();
        label6.setText("Th\u1eddi gian t\u1ea1o \u0111\u01a1n (gi\u00e2y):");
        label6.setToolTipText("");
        panel3.add((Component)label6, cc.xy(1, 11));
        this.field_TimeCreateOrder = new JTextField();
        panel3.add((Component)this.field_TimeCreateOrder, cc.xy(3, 11, CellConstraints.FILL, CellConstraints.DEFAULT));
        JLabel label7 = new JLabel();
        label7.setText("Th\u1eddi gian ch\u1eb7n spam (ph\u00fat):");
        panel3.add((Component)label7, cc.xy(1, 13));
        this.field_TimeBlockSpam = new JTextField();
        panel3.add((Component)this.field_TimeBlockSpam, cc.xy(3, 13, CellConstraints.FILL, CellConstraints.DEFAULT));
        JLabel label8 = new JLabel();
        label8.setText("Ch\u1eb7n spam sau (l\u1ea7n):");
        label8.setToolTipText("");
        panel3.add((Component)label8, cc.xy(1, 15));
        this.field_BlockSpamAfter = new JTextField();
        panel3.add((Component)this.field_BlockSpamAfter, cc.xy(3, 15, CellConstraints.FILL, CellConstraints.DEFAULT));
        JLabel label9 = new JLabel();
        label9.setText("Khi nh\u1eadn \u0111\u01a1n:");
        panel3.add((Component)label9, cc.xy(1, 23));
        JLabel label10 = new JLabel();
        label10.setText("Khi \u0111\u1eb7t c\u01b0\u1ee3c:");
        panel3.add((Component)label10, cc.xy(1, 25));
        JPanel panel4 = new JPanel();
        panel4.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel4, cc.xy(3, 23));
        this.checkbox_EnableMessageOrder = new JCheckBox();
        this.checkbox_EnableMessageOrder.setText("B\u1eadt/T\u1eaft");
        panel4.add((Component)this.checkbox_EnableMessageOrder, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MessageOrder = new JTextField();
        this.field_MessageOrder.setEnabled(false);
        panel4.add((Component)this.field_MessageOrder, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JPanel panel5 = new JPanel();
        panel5.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel5, cc.xy(3, 25));
        this.checkbox_EnableMessageBet = new JCheckBox();
        this.checkbox_EnableMessageBet.setText("B\u1eadt/T\u1eaft");
        panel5.add((Component)this.checkbox_EnableMessageBet, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MessageBet = new JTextField();
        this.field_MessageBet.setEnabled(false);
        panel5.add((Component)this.field_MessageBet, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JLabel label11 = new JLabel();
        label11.setText("Khi thua cu\u1ed9c:");
        panel3.add((Component)label11, cc.xy(1, 27));
        JLabel label12 = new JLabel();
        label12.setText("Khi th\u1eafng cu\u1ed9c:");
        panel3.add((Component)label12, cc.xy(1, 29));
        JLabel label13 = new JLabel();
        label13.setText("Khi tr\u1ea3 th\u01b0\u1edfng:");
        panel3.add((Component)label13, cc.xy(1, 31));
        JPanel panel6 = new JPanel();
        panel6.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel6, cc.xy(3, 27));
        this.checkbox_EnableMessageLose = new JCheckBox();
        this.checkbox_EnableMessageLose.setText("B\u1eadt/T\u1eaft");
        panel6.add((Component)this.checkbox_EnableMessageLose, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MessageLose = new JTextField();
        this.field_MessageLose.setEnabled(false);
        panel6.add((Component)this.field_MessageLose, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JPanel panel7 = new JPanel();
        panel7.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel7, cc.xy(3, 29));
        this.checkbox_EnableMessageWin = new JCheckBox();
        this.checkbox_EnableMessageWin.setText("B\u1eadt/T\u1eaft");
        panel7.add((Component)this.checkbox_EnableMessageWin, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MessageWin = new JTextField();
        this.field_MessageWin.setEnabled(false);
        panel7.add((Component)this.field_MessageWin, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JPanel panel8 = new JPanel();
        panel8.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel8, cc.xy(3, 31));
        this.checkbox_EnableMessageReward = new JCheckBox();
        this.checkbox_EnableMessageReward.setText("B\u1eadt/T\u1eaft");
        panel8.add((Component)this.checkbox_EnableMessageReward, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MessageReward = new JTextField();
        this.field_MessageReward.setEnabled(false);
        panel8.add((Component)this.field_MessageReward, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JPanel panel9 = new JPanel();
        panel9.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(5, 0, 5, 0), -1, -1));
        panel3.add((Component)panel9, cc.xyw(1, 21, 3, CellConstraints.FILL, CellConstraints.CENTER));
        panel9.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        JLabel label14 = new JLabel();
        label14.setText("Tin nh\u1eafn tu\u1ef3 ch\u1ecdn");
        panel9.add((Component)label14, new GridConstraints(0, 0, 1, 1, 0, 0, 0, 0, null, null, null, 0, false));
        JLabel label15 = new JLabel();
        label15.setText("L\u01b0u to\u1ea1 \u0111\u1ed9:");
        panel3.add((Component)label15, cc.xy(1, 17));
        JPanel panel10 = new JPanel();
        panel10.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel10, cc.xy(3, 17));
        this.checkbox_EnableSaveReturnPoint = new JCheckBox();
        this.checkbox_EnableSaveReturnPoint.setText("B\u1eadt/T\u1eaft");
        panel10.add((Component)this.checkbox_EnableSaveReturnPoint, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MapSaveReturnPoint = new JTextField();
        this.field_MapSaveReturnPoint.setEnabled(false);
        panel10.add((Component)this.field_MapSaveReturnPoint, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JPanel panel11 = new JPanel();
        panel11.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel11, cc.xy(3, 33));
        this.checkbox_EnableMessageOrderError = new JCheckBox();
        this.checkbox_EnableMessageOrderError.setText("B\u1eadt/T\u1eaft");
        panel11.add((Component)this.checkbox_EnableMessageOrderError, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MessageOrderError = new JTextField();
        this.field_MessageOrderError.setEnabled(false);
        panel11.add((Component)this.field_MessageOrderError, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JLabel label16 = new JLabel();
        label16.setText("Khi c\u00f3 l\u1ed7i \u0111\u01a1n:");
        panel3.add((Component)label16, cc.xy(1, 33));
        JLabel label17 = new JLabel();
        label17.setText("Gi\u1eef bot online:");
        panel3.add((Component)label17, cc.xy(1, 19));
        JPanel panel12 = new JPanel();
        panel12.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add((Component)panel12, cc.xy(3, 19));
        this.checkbox_EnableKeepBotOnline = new JCheckBox();
        this.checkbox_EnableKeepBotOnline.setText("B\u1eadt/T\u1eaft");
        panel12.add((Component)this.checkbox_EnableKeepBotOnline, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null, 0, false));
        this.field_MinuteKeepBotOnline = new JTextField();
        this.field_MinuteKeepBotOnline.setEnabled(false);
        panel12.add((Component)this.field_MinuteKeepBotOnline, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, new Dimension(150, -1), null, 0, false));
        JLabel labelStagger = new JLabel();
        labelStagger.setText("Giãn cách login (giây):");
        labelStagger.setToolTipText("Khoảng cách giữa 2 nick khi khởi động, tránh login hàng loạt bị ban IP. 0 = tắt.");
        panel3.add((Component)labelStagger, cc.xy(1, 35));
        this.field_LoginStagger = new JTextField();
        panel3.add((Component)this.field_LoginStagger, cc.xy(3, 35, CellConstraints.FILL, CellConstraints.DEFAULT));
        JLabel labelInterval = new JLabel();
        labelInterval.setText("Nhịp login tối thiểu (giây):");
        labelInterval.setToolTipText("Tối thiểu giữa 2 lần login bất kỳ trên toàn tool. 0 = tắt.");
        panel3.add((Component)labelInterval, cc.xy(1, 37));
        this.field_LoginInterval = new JTextField();
        panel3.add((Component)this.field_LoginInterval, cc.xy(3, 37, CellConstraints.FILL, CellConstraints.DEFAULT));
        JPanel panel13 = new JPanel();
        panel13.setLayout((LayoutManager)new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_Content.add((Component)panel13, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 0, null, null, null, 0, false));
        this.button_Save = new JButton();
        this.button_Save.setText("L\u01b0u");
        panel13.add((Component)this.button_Save, new GridConstraints(0, 2, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), null, 0, false));
        this.button_Close = new JButton();
        this.button_Close.setText("\u0110\u00f3ng");
        panel13.add((Component)this.button_Close, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), null, 0, false));
        Spacer spacer1 = new Spacer();
        panel13.add((Component)spacer1, new GridConstraints(0, 0, 1, 1, 0, 1, 4, 1, null, null, null, 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return this.panel_Content;
    }

    public JPanel getPanel_Content() {
        return this.panel_Content;
    }

    public JTextField getField_LoginLimit() {
        return this.field_LoginLimit;
    }

    public JTextField getField_ProtectionCode() {
        return this.field_ProtectionCode;
    }

    public JCheckBox getCheckbox_AutoRegisterProtection() {
        return this.checkbox_AutoRegisterProtection;
    }

    public JCheckBox getCheckbox_AutoOpenProtection() {
        return this.checkbox_AutoOpenProtection;
    }

    public JCheckBox getCheckbox_ProtectBot() {
        return this.checkbox_ProtectBot;
    }

    public JButton getButton_Save() {
        return this.button_Save;
    }

    public JButton getButton_Close() {
        return this.button_Close;
    }

    public JTextField getField_TimeCreateOrder() {
        return this.field_TimeCreateOrder;
    }

    public JTextField getField_TimeBlockSpam() {
        return this.field_TimeBlockSpam;
    }

    public JTextField getField_BlockSpamAfter() {
        return this.field_BlockSpamAfter;
    }

    public JTextField getField_MessageOrder() {
        return this.field_MessageOrder;
    }

    public JCheckBox getCheckbox_EnableMessageOrder() {
        return this.checkbox_EnableMessageOrder;
    }

    public JCheckBox getCheckbox_EnableMessageBet() {
        return this.checkbox_EnableMessageBet;
    }

    public JCheckBox getCheckbox_EnableMessageLose() {
        return this.checkbox_EnableMessageLose;
    }

    public JCheckBox getCheckbox_EnableMessageWin() {
        return this.checkbox_EnableMessageWin;
    }

    public JCheckBox getCheckbox_EnableMessageReward() {
        return this.checkbox_EnableMessageReward;
    }

    public JTextField getField_MessageBet() {
        return this.field_MessageBet;
    }

    public JTextField getField_MessageLose() {
        return this.field_MessageLose;
    }

    public JTextField getField_MessageWin() {
        return this.field_MessageWin;
    }

    public JTextField getField_MessageReward() {
        return this.field_MessageReward;
    }

    public JCheckBox getCheckbox_EnableSaveReturnPoint() {
        return this.checkbox_EnableSaveReturnPoint;
    }

    public JTextField getField_MapSaveReturnPoint() {
        return this.field_MapSaveReturnPoint;
    }

    public JCheckBox getCheckbox_EnableMessageOrderError() {
        return this.checkbox_EnableMessageOrderError;
    }

    public JTextField getField_MessageOrderError() {
        return this.field_MessageOrderError;
    }

    public JCheckBox getCheckbox_EnableKeepBotOnline() {
        return this.checkbox_EnableKeepBotOnline;
    }

    public JTextField getField_MinuteKeepBotOnline() {
        return this.field_MinuteKeepBotOnline;
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }

    public static SettingUI getInstance() {
        return instance;
    }
}

