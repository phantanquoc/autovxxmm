/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.intellij.uiDesigner.core.GridConstraints
 *  com.intellij.uiDesigner.core.GridLayoutManager
 *  com.intellij.uiDesigner.core.Spacer
 */
package ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import constants.Strings;
import core.model.Bot;
import core.model.Server;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import main.Application;
import service.BotService;
import service.ServerService;
import ui.BlockUI;
import ui.SettingUI;
import ui.listeners.ApplicationUIActionListener;
import ui.listeners.ApplicationWindowListener;
import ui.models.CollectTableModel;
import ui.models.OrderTableModel;
import ui.models.TransferTableModel;
import ui.renderers.CollectTableCellRenderer;
import ui.renderers.OrderTableCellRenderer;
import ui.renderers.TransferTableCellRenderer;
import utils.FileUtils;
import utils.Res;
import utils.TextUtils;

public class ApplicationUI
extends JFrame {
    private static final Logger logger = Logger.getLogger(ApplicationUI.class.getName());
    private static ApplicationUI instance;
    private JPanel panel_Main;
    private JPanel panel_First;
    private JPanel panel_Second;
    private JPanel panel_Third;
    private JTable table_Order;
    private JPanel panel_First_RightInnerBottomLeft;
    private JButton button_NextPage;
    private JButton button_PreviousPage;
    private JComboBox combobox_SelectPageElement;
    private JPanel panel_First_Left;
    private JScrollPane panel_First_TopLeft;
    private JPanel panel_First_LeftInnerBottomLeft;
    private JPanel panel_First_BottomLeft;
    private JPanel panel_First_Right;
    private JPanel panel_First_TopRight;
    private JLabel label_OperatingTime;
    private JLabel label_ExpiredTime;
    private JLabel label_ActiveBot;
    private JPanel panel_First_RightInnerBottomLeft_Top;
    private JPanel panel_First_Center_Right;
    private JTable table_Transfer;
    private JTable table_Collect;
    private JButton button_BlockList;
    private JButton button_Setting;
    private JButton button_TransferCoin;
    private JButton button_ImportBotTransfer;
    private JButton button_CollectCoin;
    private JButton button_ImportBotCollect;
    private JScrollPane panel_Second_Left;
    private JPanel panel_Second_Right;
    private JScrollPane panel_Third_Left;
    private JPanel panel_Third_Right;
    private JLabel label_PageDetails;
    private JLabel label_Map;
    private JLabel label_Zone;
    private JLabel label_X;
    private JLabel label_Y;
    private JLabel label_Kins;
    private JLabel label_Coins;
    private JLabel label_Golds;
    private JLabel label_Account;
    private JLabel label_Character;
    private JButton button_Update;
    private final ApplicationUIActionListener uiActionListener;
    private final OrderTableModel orderTableModel;
    private final TransferTableModel transferTableModel;
    private final CollectTableModel collectTableModel;
    private int orderTableSelectedRow = -1;
    private Bot selectedBotOrder = null;
    private int transferTableSelectedRow = -1;
    private int collectTableSelectedRow = -1;
    private long timeStart;
    private int pageElement = 20;
    private int currentPage = 1;
    private int totalPage = 0;
    private boolean pageChanged = false;
    private boolean botOrderChanged = false;
    private boolean onCollectCoin = false;
    private boolean onTransferCoin = false;

    public ApplicationUI() {
        this.$$$setupUI$$$();
        this.setContentPane(this.panel_Main);
        this.setTitle(Strings.MAIN_TITLE);
        this.setDefaultCloseOperation(0);
        this.setSize(1200, 820);
        this.setLocationRelativeTo(null);
        DefaultListSelectionModel tableOrderSelectionModel = new DefaultListSelectionModel();
        tableOrderSelectionModel.setSelectionMode(0);
        tableOrderSelectionModel.addListSelectionListener(e -> {
            int selectedRow = this.table_Order.getSelectedRow();
            if (selectedRow >= 0) {
                this.orderTableSelectedRow = selectedRow;
                int index = (this.currentPage - 1) * this.pageElement + selectedRow;
                this.selectedBotOrder = BotService.getInstance().getBotOrders().get(index);
            }
        });
        this.orderTableModel = new OrderTableModel(this, this.table_Order.getColumnModel());
        this.table_Order.setDefaultRenderer(Object.class, new OrderTableCellRenderer(this));
        this.table_Order.setCellSelectionEnabled(false);
        this.table_Order.setRowSelectionAllowed(true);
        this.table_Order.setSelectionModel(tableOrderSelectionModel);
        this.table_Order.setModel(this.orderTableModel);
        this.table_Order.getTableHeader().setReorderingAllowed(false);
        DefaultListSelectionModel tableTransferSelectionModel = new DefaultListSelectionModel();
        tableTransferSelectionModel.setSelectionMode(0);
        tableTransferSelectionModel.addListSelectionListener(e -> {
            int selectedRow = this.table_Transfer.getSelectedRow();
            if (selectedRow >= 0) {
                this.transferTableSelectedRow = selectedRow;
            }
        });
        this.transferTableModel = new TransferTableModel(this, this.table_Transfer.getColumnModel());
        this.table_Transfer.setDefaultRenderer(Object.class, new TransferTableCellRenderer(this));
        this.table_Transfer.setCellSelectionEnabled(false);
        this.table_Transfer.setRowSelectionAllowed(true);
        this.table_Transfer.setSelectionModel(tableTransferSelectionModel);
        this.table_Transfer.setModel(this.transferTableModel);
        this.table_Transfer.getTableHeader().setReorderingAllowed(false);
        DefaultListSelectionModel tableCollectSelectionModel = new DefaultListSelectionModel();
        tableCollectSelectionModel.setSelectionMode(0);
        tableCollectSelectionModel.addListSelectionListener(e -> {
            int selectedRow = this.table_Collect.getSelectedRow();
            if (selectedRow >= 0) {
                this.collectTableSelectedRow = selectedRow;
            }
        });
        this.collectTableModel = new CollectTableModel(this, this.table_Collect.getColumnModel());
        this.table_Collect.setDefaultRenderer(Object.class, new CollectTableCellRenderer(this));
        this.table_Collect.setCellSelectionEnabled(false);
        this.table_Collect.setRowSelectionAllowed(true);
        this.table_Collect.setSelectionModel(tableCollectSelectionModel);
        this.table_Collect.setModel(this.collectTableModel);
        this.table_Collect.getTableHeader().setReorderingAllowed(false);
        this.combobox_SelectPageElement.addActionListener(e -> {
            String selectedPageElement = (String)this.combobox_SelectPageElement.getSelectedItem();
            this.pageElement = Integer.parseInt(selectedPageElement);
            this.calculateTotalPage();
            this.updatePageDetails();
            this.pageChanged = true;
        });
        this.combobox_SelectPageElement.setSelectedItem(String.valueOf(this.pageElement));
        this.button_PreviousPage.addActionListener(e -> {
            if (this.currentPage > 1) {
                this.updatePage(this.currentPage - 1);
            }
        });
        this.button_NextPage.addActionListener(e -> {
            if (this.currentPage < this.totalPage) {
                this.updatePage(this.currentPage + 1);
            }
        });
        this.button_BlockList.addActionListener(e -> BlockUI.getInstance().open());
        this.button_Setting.addActionListener(e -> SettingUI.getInstance().open());
        this.button_TransferCoin.addActionListener(e -> {
            List<Bot> botTransfers = BotService.getInstance().getBotTransfers();
            if (!this.onTransferCoin && botTransfers.isEmpty()) {
                Application.alert(this, "B\u1ea1n ch\u01b0a c\u00f3 t\u00e0i kho\u1ea3n b\u01a1m xu n\u00e0o!");
                return;
            }
            this.onTransferCoin = !this.onTransferCoin;
            this.button_TransferCoin.setText(!this.onTransferCoin ? "B\u01a1m xu" : "D\u1eebng b\u01a1m xu");
            botTransfers.forEach(bot -> bot.getScreen().transferScreen().onTransfer(this.onTransferCoin));
        });
        this.button_ImportBotTransfer.addActionListener(e -> {
            String path = FileUtils.chooseFile(this, new FileNameExtensionFilter("Text files", "txt"));
            if (path != null) {
                try {
                    ArrayList<Bot> botTransfers = new ArrayList<Bot>();
                    int id = 0;
                    String st = FileUtils.readAsString(path);
                    String[] lines = st.split("\r\n|\r|\n");
                    for (int i = 0; i < lines.length; ++i) {
                        String message;
                        String s = lines[i].trim().replaceAll("\ufeff", "");
                        if (s.isEmpty() || TextUtils.isComment(s)) continue;
                        Server server = ServerService.getInstance().getServer(TextUtils.getInteger(s, "SV"));
                        String account = TextUtils.getText(s, "TK");
                        String password = TextUtils.getText(s, "MK");
                        String character = TextUtils.getText(s, "NV");
                        if (server == null) {
                            message = String.format("D\u00f2ng %d: M\u00e1y ch\u1ee7 kh\u00f4ng h\u1ee3p l\u1ec7!", i + 1);
                            Application.error(this, message);
                            return;
                        }
                        if (account == null) {
                            message = String.format("D\u00f2ng %d: T\u00e0i kho\u1ea3n kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!", i + 1);
                            Application.error(this, message);
                            return;
                        }
                        if (password == null) {
                            message = String.format("D\u00f2ng %d: M\u1eadt kh\u1ea9u kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!", i + 1);
                            Application.error(this, message);
                            return;
                        }
                        Bot bot = Bot.createBotTransfer(id++, server, account, password, character);
                        botTransfers.add(bot);
                    }
                    BotService.getInstance().clear(1);
                    BotService.getInstance().getBotTransfers().addAll(botTransfers);
                    botTransfers.forEach(Bot::createConnect);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                    Application.error(this, ex.getMessage());
                }
            }
        });
        this.button_CollectCoin.addActionListener(e -> {
            List<Bot> botCollects = BotService.getInstance().getBotCollects();
            if (!this.onCollectCoin && botCollects.isEmpty()) {
                Application.alert(this, "B\u1ea1n ch\u01b0a c\u00f3 t\u00e0i kho\u1ea3n gom xu n\u00e0o!");
                return;
            }
            this.onCollectCoin = !this.onCollectCoin;
            this.button_CollectCoin.setText(!this.onCollectCoin ? "Gom xu" : "D\u1eebng gom xu");
            botCollects.forEach(bot -> bot.getScreen().collectScreen().onCollect(this.onCollectCoin));
        });
        this.button_ImportBotCollect.addActionListener(e -> {
            String path = FileUtils.chooseFile(this, new FileNameExtensionFilter("Text files", "txt"));
            if (path != null) {
                try {
                    ArrayList<Bot> botCollects = new ArrayList<Bot>();
                    int id = 0;
                    String st = FileUtils.readAsString(path);
                    String[] lines = st.split("\r\n|\r|\n");
                    for (int i = 0; i < lines.length; ++i) {
                        String message;
                        String s = lines[i].trim().replaceAll("\ufeff", "");
                        if (s.isEmpty() || TextUtils.isComment(s)) continue;
                        Server server = ServerService.getInstance().getServer(TextUtils.getInteger(s, "SV"));
                        String account = TextUtils.getText(s, "TK");
                        String password = TextUtils.getText(s, "MK");
                        String character = TextUtils.getText(s, "NV");
                        if (server == null) {
                            message = String.format("D\u00f2ng %d: M\u00e1y ch\u1ee7 kh\u00f4ng h\u1ee3p l\u1ec7!", i + 1);
                            Application.error(this, message);
                            return;
                        }
                        if (account == null) {
                            message = String.format("D\u00f2ng %d: T\u00e0i kho\u1ea3n kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!", i + 1);
                            Application.error(this, message);
                            return;
                        }
                        if (password == null) {
                            message = String.format("D\u00f2ng %d: M\u1eadt kh\u1ea9u kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!", i + 1);
                            Application.error(this, message);
                            return;
                        }
                        Bot bot = Bot.createBotCollect(id++, server, account, password, character);
                        botCollects.add(bot);
                    }
                    BotService.getInstance().clear(2);
                    BotService.getInstance().getBotCollects().addAll(botCollects);
                    botCollects.forEach(Bot::createConnect);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Application.error(this, ex.toString());
                }
            }
        });
        this.button_Update.setVisible(false);
        this.addWindowListener(new ApplicationWindowListener(this));
        this.uiActionListener = new ApplicationUIActionListener(this);
        Timer timer = new Timer(1000, this.uiActionListener);
        timer.start();
        this.timeStart = Res.t();
    }

    private void updatePage(int page) {
        this.currentPage = page;
        this.orderTableSelectedRow = -1;
        this.selectedBotOrder = null;
        this.updatePageDetails();
        this.pageChanged = true;
    }

    private void calculateTotalPage() {
        this.totalPage = (int)Math.ceil((double)BotService.getInstance().getBotOrders().size() / (double)this.pageElement);
        if (this.currentPage > this.totalPage) {
            this.currentPage = 1;
        }
    }

    public void calculateTotalPageAndUpdate() {
        this.calculateTotalPage();
        this.updatePageDetails();
    }

    private void updatePageDetails() {
        String text = String.format("Trang %d tr\u00ean %d", this.currentPage > this.totalPage ? 0 : this.currentPage, this.totalPage);
        this.label_PageDetails.setText(text);
    }

    public static ApplicationUI getInstance() {
        if (instance == null) {
            instance = new ApplicationUI();
        }
        return instance;
    }

    private void $$$setupUI$$$() {
        this.panel_Main = new JPanel();
        this.panel_Main.setLayout((LayoutManager)new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        this.panel_Main.setMaximumSize(new Dimension(-1, -1));
        this.panel_Main.setMinimumSize(new Dimension(1200, 800));
        this.panel_Main.setPreferredSize(new Dimension(1200, 1100));
        this.panel_First = new JPanel();
        this.panel_First.setLayout((LayoutManager)new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_Main.add((Component)this.panel_First, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));
        this.panel_First_Right = new JPanel();
        this.panel_First_Right.setLayout(new BorderLayout(0, 0));
        this.panel_First.add((Component)this.panel_First_Right, new GridConstraints(0, 1, 2, 1, 0, 3, 0, 3, new Dimension(250, -1), new Dimension(280, -1), null, 0, false));
        this.panel_First_TopRight = new JPanel();
        this.panel_First_TopRight.setLayout((LayoutManager)new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        this.panel_First_Right.add((Component)this.panel_First_TopRight, "North");
        this.panel_First_TopRight.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        JLabel label1 = new JLabel();
        label1.setText("Th\u1eddi gian ho\u1ea1t \u0111\u1ed9ng:");
        this.panel_First_TopRight.add((Component)label1, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, new Dimension(140, 18), new Dimension(140, 18), new Dimension(140, 18), 0, false));
        this.label_OperatingTime = new JLabel();
        this.label_OperatingTime.setText("00:00:00");
        this.panel_First_TopRight.add((Component)this.label_OperatingTime, new GridConstraints(0, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(110, 18), null, 0, false));
        JLabel label2 = new JLabel();
        label2.setText("Th\u1eddi gian h\u1ebft h\u1ea1n:");
        this.panel_First_TopRight.add((Component)label2, new GridConstraints(1, 0, 1, 1, 8, 0, 0, 0, new Dimension(140, 18), new Dimension(140, 18), new Dimension(140, 18), 0, false));
        this.label_ExpiredTime = new JLabel();
        this.label_ExpiredTime.setText("infinitive");
        this.panel_First_TopRight.add((Component)this.label_ExpiredTime, new GridConstraints(1, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(110, 18), null, 0, false));
        JLabel label3 = new JLabel();
        label3.setText("T\u00e0i kho\u1ea3n ho\u1ea1t \u0111\u1ed9ng:");
        this.panel_First_TopRight.add((Component)label3, new GridConstraints(2, 0, 1, 1, 8, 0, 0, 0, new Dimension(140, 18), new Dimension(140, 18), new Dimension(140, 18), 0, false));
        this.label_ActiveBot = new JLabel();
        this.label_ActiveBot.setText("00/00");
        this.panel_First_TopRight.add((Component)this.label_ActiveBot, new GridConstraints(2, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(110, 18), null, 0, false));
        this.panel_First_Center_Right = new JPanel();
        this.panel_First_Center_Right.setLayout((LayoutManager)new GridLayoutManager(12, 2, new Insets(5, 5, 5, 5), -1, -1));
        this.panel_First_Right.add((Component)this.panel_First_Center_Right, "Center");
        this.panel_First_Center_Right.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        JLabel label4 = new JLabel();
        label4.setText("Map:");
        this.panel_First_Center_Right.add((Component)label4, new GridConstraints(2, 0, 1, 1, 8, 0, 0, 0, new Dimension(140, 18), new Dimension(140, 18), new Dimension(140, 18), 0, false));
        this.label_Map = new JLabel();
        this.label_Map.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Map, new GridConstraints(2, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(100, -1), null, 0, false));
        JLabel label5 = new JLabel();
        label5.setText("X:");
        this.panel_First_Center_Right.add((Component)label5, new GridConstraints(4, 0, 1, 1, 8, 0, 0, 0, new Dimension(140, 18), new Dimension(140, 18), new Dimension(140, 18), 0, false));
        this.label_Zone = new JLabel();
        this.label_Zone.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Zone, new GridConstraints(3, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(100, -1), null, 0, false));
        this.label_X = new JLabel();
        this.label_X.setText("");
        this.panel_First_Center_Right.add((Component)this.label_X, new GridConstraints(4, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(100, -1), null, 0, false));
        this.label_Y = new JLabel();
        this.label_Y.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Y, new GridConstraints(5, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(100, -1), null, 0, false));
        JLabel label6 = new JLabel();
        label6.setText("Y\u00ean:");
        this.panel_First_Center_Right.add((Component)label6, new GridConstraints(6, 0, 1, 1, 8, 0, 0, 0, new Dimension(140, 18), new Dimension(140, 18), new Dimension(140, 18), 0, false));
        this.label_Kins = new JLabel();
        this.label_Kins.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Kins, new GridConstraints(6, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(100, -1), null, 0, false));
        this.label_Coins = new JLabel();
        this.label_Coins.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Coins, new GridConstraints(7, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(100, -1), null, 0, false));
        this.label_Golds = new JLabel();
        this.label_Golds.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Golds, new GridConstraints(8, 1, 1, 1, 8, 0, 5, 0, null, new Dimension(100, -1), null, 0, false));
        Spacer spacer1 = new Spacer();
        this.panel_First_Center_Right.add((Component)spacer1, new GridConstraints(9, 1, 1, 1, 0, 2, 1, 4, null, null, null, 0, false));
        Spacer spacer2 = new Spacer();
        this.panel_First_Center_Right.add((Component)spacer2, new GridConstraints(9, 0, 1, 1, 0, 2, 1, 4, null, null, null, 0, false));
        this.button_BlockList = new JButton();
        this.button_BlockList.setText("Danh s\u00e1ch ch\u1eb7n");
        this.panel_First_Center_Right.add((Component)this.button_BlockList, new GridConstraints(10, 0, 1, 2, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), new Dimension(-1, 22), 0, false));
        this.button_Setting = new JButton();
        this.button_Setting.setText("C\u00e0i \u0111\u1eb7t");
        this.panel_First_Center_Right.add((Component)this.button_Setting, new GridConstraints(11, 0, 1, 2, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), new Dimension(-1, 22), 0, false));
        JLabel label7 = new JLabel();
        label7.setText("T\u00e0i kho\u1ea3n:");
        this.panel_First_Center_Right.add((Component)label7, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JLabel label8 = new JLabel();
        label8.setText("Nh\u00e2n v\u1eadt:");
        this.panel_First_Center_Right.add((Component)label8, new GridConstraints(1, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JLabel label9 = new JLabel();
        label9.setText("Khu:");
        this.panel_First_Center_Right.add((Component)label9, new GridConstraints(3, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JLabel label10 = new JLabel();
        label10.setText("Y:");
        this.panel_First_Center_Right.add((Component)label10, new GridConstraints(5, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JLabel label11 = new JLabel();
        label11.setText("Xu:");
        this.panel_First_Center_Right.add((Component)label11, new GridConstraints(7, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JLabel label12 = new JLabel();
        label12.setText("L\u01b0\u1ee3ng:");
        this.panel_First_Center_Right.add((Component)label12, new GridConstraints(8, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        this.label_Character = new JLabel();
        this.label_Character.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Character, new GridConstraints(1, 1, 1, 1, 8, 0, 0, 0, null, new Dimension(100, -1), null, 0, false));
        this.label_Account = new JLabel();
        this.label_Account.setText("");
        this.panel_First_Center_Right.add((Component)this.label_Account, new GridConstraints(0, 1, 1, 1, 8, 0, 0, 0, null, new Dimension(100, -1), null, 0, false));
        this.panel_First_Left = new JPanel();
        this.panel_First_Left.setLayout((LayoutManager)new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_First.add((Component)this.panel_First_Left, new GridConstraints(0, 0, 2, 1, 0, 3, 3, 3, null, null, null, 0, false));
        this.panel_First_TopLeft = new JScrollPane();
        this.panel_First_Left.add((Component)this.panel_First_TopLeft, new GridConstraints(0, 0, 1, 1, 0, 3, 5, 5, new Dimension(580, 300), new Dimension(920, 300), null, 0, false));
        this.panel_First_TopLeft.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        this.table_Order = new JTable();
        this.table_Order.setSelectionBackground(new Color(-1));
        this.panel_First_TopLeft.setViewportView(this.table_Order);
        this.panel_First_BottomLeft = new JPanel();
        this.panel_First_BottomLeft.setLayout(new BorderLayout(0, 0));
        this.panel_First_Left.add((Component)this.panel_First_BottomLeft, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));
        this.panel_First_RightInnerBottomLeft = new JPanel();
        this.panel_First_RightInnerBottomLeft.setLayout(new BorderLayout(0, 0));
        this.panel_First_BottomLeft.add((Component)this.panel_First_RightInnerBottomLeft, "East");
        this.panel_First_RightInnerBottomLeft_Top = new JPanel();
        this.panel_First_RightInnerBottomLeft_Top.setLayout((LayoutManager)new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_First_RightInnerBottomLeft.add((Component)this.panel_First_RightInnerBottomLeft_Top, "North");
        this.button_NextPage = new JButton();
        this.button_NextPage.setText(">");
        this.panel_First_RightInnerBottomLeft_Top.add((Component)this.button_NextPage, new GridConstraints(0, 2, 1, 1, 0, 1, 3, 0, new Dimension(45, 22), new Dimension(45, 22), new Dimension(45, 22), 0, false));
        this.button_PreviousPage = new JButton();
        this.button_PreviousPage.setText("<");
        this.panel_First_RightInnerBottomLeft_Top.add((Component)this.button_PreviousPage, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 0, new Dimension(45, 22), new Dimension(45, 22), new Dimension(45, 22), 0, false));
        this.combobox_SelectPageElement = new JComboBox();
        DefaultComboBoxModel<String> defaultComboBoxModel1 = new DefaultComboBoxModel<String>();
        defaultComboBoxModel1.addElement("1");
        defaultComboBoxModel1.addElement("5");
        defaultComboBoxModel1.addElement("10");
        defaultComboBoxModel1.addElement("20");
        defaultComboBoxModel1.addElement("30");
        defaultComboBoxModel1.addElement("50");
        this.combobox_SelectPageElement.setModel(defaultComboBoxModel1);
        this.panel_First_RightInnerBottomLeft_Top.add((Component)this.combobox_SelectPageElement, new GridConstraints(0, 0, 1, 1, 8, 1, 2, 0, new Dimension(60, 20), new Dimension(60, 20), new Dimension(60, 20), 0, false));
        JPanel panel1 = new JPanel();
        panel1.setLayout((LayoutManager)new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_First_RightInnerBottomLeft.add((Component)panel1, "Center");
        this.label_PageDetails = new JLabel();
        this.label_PageDetails.setText("Trang:");
        panel1.add((Component)this.label_PageDetails, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, null, null, null, 0, false));
        Spacer spacer3 = new Spacer();
        panel1.add((Component)spacer3, new GridConstraints(1, 0, 1, 1, 0, 2, 1, 4, null, null, null, 0, false));
        this.panel_First_LeftInnerBottomLeft = new JPanel();
        this.panel_First_LeftInnerBottomLeft.setLayout((LayoutManager)new GridLayoutManager(4, 2, new Insets(5, 5, 5, 5), -1, -1));
        this.panel_First_LeftInnerBottomLeft.setEnabled(true);
        this.panel_First_BottomLeft.add((Component)this.panel_First_LeftInnerBottomLeft, "West");
        this.panel_First_LeftInnerBottomLeft.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, this.$$$getFont$$$(null, -1, -1, this.panel_First_LeftInnerBottomLeft.getFont()), new Color(-16777216)));
        JLabel label13 = new JLabel();
        label13.setText("Ch\u1edd \u0111\u1eb7t c\u01b0\u1ee3c:");
        this.panel_First_LeftInnerBottomLeft.add((Component)label13, new GridConstraints(1, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JLabel label14 = new JLabel();
        label14.setText("\u0110\u00e3 \u0111\u1eb7t c\u01b0\u1ee3c:");
        this.panel_First_LeftInnerBottomLeft.add((Component)label14, new GridConstraints(2, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JLabel label15 = new JLabel();
        label15.setText("\u0110\u00e3 th\u1eafng c\u01b0\u1ee3c:");
        this.panel_First_LeftInnerBottomLeft.add((Component)label15, new GridConstraints(3, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JPanel panel2 = new JPanel();
        panel2.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setBackground(new Color(-256));
        this.panel_First_LeftInnerBottomLeft.add((Component)panel2, new GridConstraints(1, 1, 1, 1, 0, 0, 3, 3, new Dimension(15, 15), new Dimension(15, 15), new Dimension(15, 15), 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-6579301)), null, 0, 0, null, null));
        JPanel panel3 = new JPanel();
        panel3.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setBackground(new Color(-16776961));
        this.panel_First_LeftInnerBottomLeft.add((Component)panel3, new GridConstraints(2, 1, 1, 1, 0, 0, 3, 3, new Dimension(15, 15), new Dimension(15, 15), new Dimension(15, 15), 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-6579301)), null, 0, 0, null, null));
        JPanel panel4 = new JPanel();
        panel4.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.setBackground(new Color(-65536));
        this.panel_First_LeftInnerBottomLeft.add((Component)panel4, new GridConstraints(3, 1, 1, 1, 0, 0, 3, 3, new Dimension(15, 15), new Dimension(15, 15), new Dimension(15, 15), 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-6579301)), null, 0, 0, null, null));
        JLabel label16 = new JLabel();
        label16.setText("\u0110ang ch\u1ecdn:");
        this.panel_First_LeftInnerBottomLeft.add((Component)label16, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        JPanel panel5 = new JPanel();
        panel5.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setBackground(new Color(-16711681));
        this.panel_First_LeftInnerBottomLeft.add((Component)panel5, new GridConstraints(0, 1, 1, 1, 0, 0, 3, 3, new Dimension(15, 15), new Dimension(15, 15), new Dimension(15, 15), 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-6579301)), null, 0, 0, null, null));
        JPanel panel6 = new JPanel();
        panel6.setLayout((LayoutManager)new GridLayoutManager(2, 2, new Insets(0, 5, 0, 0), -1, -1));
        this.panel_First_BottomLeft.add((Component)panel6, "Center");
        Spacer spacer4 = new Spacer();
        panel6.add((Component)spacer4, new GridConstraints(0, 1, 1, 1, 0, 1, 4, 1, null, null, null, 0, false));
        Spacer spacer5 = new Spacer();
        panel6.add((Component)spacer5, new GridConstraints(1, 1, 1, 1, 0, 2, 1, 4, null, null, null, 0, false));
        this.button_Update = new JButton();
        this.button_Update.setText("C\u1eadp nh\u1eadt ");
        panel6.add((Component)this.button_Update, new GridConstraints(0, 0, 1, 1, 0, 1, 0, 0, null, new Dimension(-1, 22), new Dimension(-1, 22), 0, false));
        this.panel_Second = new JPanel();
        this.panel_Second.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_Second.setEnabled(true);
        this.panel_Second.setVisible(true);
        this.panel_Main.add((Component)this.panel_Second, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 1, null, null, null, 0, false));
        this.panel_Second_Left = new JScrollPane();
        this.panel_Second_Left.setEnabled(true);
        this.panel_Second.add((Component)this.panel_Second_Left, new GridConstraints(0, 0, 1, 1, 0, 3, 5, 5, new Dimension(580, 150), new Dimension(920, 150), null, 0, false));
        this.panel_Second_Left.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        this.table_Transfer = new JTable();
        this.panel_Second_Left.setViewportView(this.table_Transfer);
        this.panel_Second_Right = new JPanel();
        this.panel_Second_Right.setLayout((LayoutManager)new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        this.panel_Second_Right.setEnabled(true);
        this.panel_Second.add((Component)this.panel_Second_Right, new GridConstraints(0, 1, 1, 1, 0, 3, 0, 0, new Dimension(250, -1), new Dimension(280, -1), null, 0, false));
        this.panel_Second_Right.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        this.button_TransferCoin = new JButton();
        this.button_TransferCoin.setText("B\u01a1m xu");
        this.panel_Second_Right.add((Component)this.button_TransferCoin, new GridConstraints(0, 0, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), new Dimension(-1, 22), 0, false));
        this.button_ImportBotTransfer = new JButton();
        this.button_ImportBotTransfer.setText("Nh\u1eadp danh s\u00e1ch bot b\u01a1m xu");
        this.panel_Second_Right.add((Component)this.button_ImportBotTransfer, new GridConstraints(1, 0, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), new Dimension(-1, 22), 0, false));
        Spacer spacer6 = new Spacer();
        this.panel_Second_Right.add((Component)spacer6, new GridConstraints(2, 0, 1, 1, 0, 2, 1, 4, null, null, null, 0, false));
        this.panel_Third = new JPanel();
        this.panel_Third.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_Main.add((Component)this.panel_Third, new GridConstraints(2, 0, 1, 1, 0, 3, 3, 1, null, null, null, 0, false));
        this.panel_Third_Left = new JScrollPane();
        this.panel_Third.add((Component)this.panel_Third_Left, new GridConstraints(0, 0, 1, 1, 0, 3, 5, 5, new Dimension(580, 150), new Dimension(920, 150), null, 0, false));
        this.panel_Third_Left.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        this.table_Collect = new JTable();
        this.panel_Third_Left.setViewportView(this.table_Collect);
        this.panel_Third_Right = new JPanel();
        this.panel_Third_Right.setLayout((LayoutManager)new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        this.panel_Third.add((Component)this.panel_Third_Right, new GridConstraints(0, 1, 1, 1, 0, 3, 0, 0, new Dimension(250, -1), new Dimension(280, -1), null, 0, false));
        this.panel_Third_Right.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, 0, 0, null, null));
        this.button_CollectCoin = new JButton();
        this.button_CollectCoin.setText("Gom xu");
        this.panel_Third_Right.add((Component)this.button_CollectCoin, new GridConstraints(0, 0, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), new Dimension(-1, 22), 0, false));
        this.button_ImportBotCollect = new JButton();
        this.button_ImportBotCollect.setText("Nh\u1eadp danh s\u00e1ch bot gom xu");
        this.panel_Third_Right.add((Component)this.button_ImportBotCollect, new GridConstraints(1, 0, 1, 1, 0, 1, 3, 0, new Dimension(-1, 22), new Dimension(-1, 22), new Dimension(-1, 22), 0, false));
        Spacer spacer7 = new Spacer();
        this.panel_Third_Right.add((Component)spacer7, new GridConstraints(2, 0, 1, 1, 0, 2, 1, 4, null, null, null, 0, false));
    }

    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        Font testFont;
        if (currentFont == null) {
            return null;
        }
        String resultName = fontName == null ? currentFont.getName() : ((testFont = new Font(fontName, 0, 10)).canDisplay('a') && testFont.canDisplay('1') ? fontName : currentFont.getName());
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    public JComponent $$$getRootComponent$$$() {
        return this.panel_Main;
    }

    public JLabel getLabel_OperatingTime() {
        return this.label_OperatingTime;
    }

    public JLabel getLabel_ActiveBot() {
        return this.label_ActiveBot;
    }

    public JLabel getLabel_Map() {
        return this.label_Map;
    }

    public JLabel getLabel_Zone() {
        return this.label_Zone;
    }

    public JLabel getLabel_X() {
        return this.label_X;
    }

    public JLabel getLabel_Y() {
        return this.label_Y;
    }

    public JLabel getLabel_Kins() {
        return this.label_Kins;
    }

    public JLabel getLabel_Coins() {
        return this.label_Coins;
    }

    public JLabel getLabel_Golds() {
        return this.label_Golds;
    }

    public JLabel getLabel_Account() {
        return this.label_Account;
    }

    public JLabel getLabel_Character() {
        return this.label_Character;
    }

    public OrderTableModel getOrderTableModel() {
        return this.orderTableModel;
    }

    public TransferTableModel getTransferTableModel() {
        return this.transferTableModel;
    }

    public CollectTableModel getCollectTableModel() {
        return this.collectTableModel;
    }

    public int getOrderTableSelectedRow() {
        return this.orderTableSelectedRow;
    }

    public Bot getSelectedBotOrder() {
        return this.selectedBotOrder;
    }

    public int getTransferTableSelectedRow() {
        return this.transferTableSelectedRow;
    }

    public int getCollectTableSelectedRow() {
        return this.collectTableSelectedRow;
    }

    public long getTimeStart() {
        return this.timeStart;
    }

    public int getPageElement() {
        return this.pageElement;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public int getTotalPage() {
        return this.totalPage;
    }

    public boolean isPageChanged() {
        return this.pageChanged;
    }

    public void setPageChanged(boolean pageChanged) {
        this.pageChanged = pageChanged;
    }

    public boolean isBotOrderChanged() {
        return this.botOrderChanged;
    }

    public void setBotOrderChanged(boolean botOrderChanged) {
        this.botOrderChanged = botOrderChanged;
    }
}

