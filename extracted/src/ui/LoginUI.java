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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import main.Application;
import network.http.Request;
import service.BotService;
import service.OrderService;
import service.ServerService;
import ui.ApplicationUI;
import utils.FileUtils;

public class LoginUI
extends JFrame {
    private static final String CONFIGURATION_PATH = "records/credentials.txt";
    private static final List<ClientOption> CLIENT_OPTIONS = Arrays.asList(new ClientOption("Ch\u01b0a ch\u1ecdn", 0), new ClientOption("Client 1", 1), new ClientOption("Client 2", 2), new ClientOption("Client 3", 3), new ClientOption("Client 4", 4), new ClientOption("Client 5", 5), new ClientOption("Client 6", 6), new ClientOption("Client 7", 7), new ClientOption("Client 8", 8), new ClientOption("Client 9", 9), new ClientOption("Client 10", 10));
    private static LoginUI instance;
    private JPanel panel_Main;
    private JPanel panel_InnerMain;
    private JPanel panel_InnerBottom;
    private JPanel panel_InnerCenter;
    private JButton button_Exit;
    private JButton button_Login;
    private JLabel label_Username;
    private JLabel label_Password;
    private JTextField field_Username;
    private JPasswordField field_Password;
    private JCheckBox checkbox_SplitClients;
    private JComboBox<ClientOption> combobox_SelectClient;

    public LoginUI() {
        this.$$$setupUI$$$();
        CLIENT_OPTIONS.forEach(item -> this.combobox_SelectClient.addItem((ClientOption)item));
        this.setContentPane(this.panel_Main);
        this.setTitle(Strings.LOGIN_TITLE);
        this.setDefaultCloseOperation(3);
        this.setSize(300, 200);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.loadConfiguration();
        this.button_Login.addActionListener(e -> {
            String username = this.field_Username.getText().trim();
            String password = new String(this.field_Password.getPassword()).trim();
            boolean splitClients = this.checkbox_SplitClients.isSelected();
            ClientOption selectedClient = (ClientOption)this.combobox_SelectClient.getSelectedItem();
            if (username.isEmpty()) {
                Application.alert("T\u00e0i kho\u1ea3n kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!");
                return;
            }
            if (password.isEmpty()) {
                Application.alert("M\u1eadt kh\u1ea9u kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng!");
                return;
            }
            if (splitClients && selectedClient.getValue() == 0) {
                Application.alert("Ch\u01b0a ch\u1ecdn client!");
                return;
            }
            this.saveConfiguration();
            Application.isSplitClient = splitClients;
            Application.client = selectedClient.getValue();
            this.handleLogin(username, password);
        });
        this.button_Exit.addActionListener(e -> System.exit(0));
        this.checkbox_SplitClients.addActionListener(e -> this.updateEnableState());
    }

    public static LoginUI getInstance() {
        if (instance == null) {
            instance = new LoginUI();
        }
        return instance;
    }

    private void handleLogin(String username, String password) {
        Request.setUsername(username);
        Request.setPassword(password);
        this.setVisible(false);
        if (Request.authenticate()) {
            new Thread(() -> {
                ServerService.getInstance().loadServers();
                BotService.getInstance().initialize();
                OrderService.getInstance().initialize();
            }).start();
            ApplicationUI.getInstance().setVisible(true);
            Application.info(ApplicationUI.getInstance(), "\u0110\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng!");
        } else {
            this.setVisible(true);
        }
    }

    private void updateEnableState() {
        this.combobox_SelectClient.setEnabled(this.checkbox_SplitClients.isSelected());
    }

    public void loadConfiguration() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(FileUtils.read(CONFIGURATION_PATH));
            DataInputStream dis = new DataInputStream(bais);
            String username = dis.readUTF();
            String password = new String(Base64.getDecoder().decode(dis.readUTF()));
            boolean splitClients = dis.readBoolean();
            int selectedClient = dis.readInt();
            dis.close();
            bais.close();
            this.field_Username.setText(username);
            this.field_Password.setText(password);
            this.checkbox_SplitClients.setSelected(splitClients);
            CLIENT_OPTIONS.forEach(option -> {
                if (option.getValue() == selectedClient) {
                    this.combobox_SelectClient.setSelectedItem(option);
                }
            });
            this.updateEnableState();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveConfiguration() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(this.field_Username.getText());
            String password = new String(this.field_Password.getPassword());
            dos.writeUTF(new String(Base64.getEncoder().encode(password.getBytes())));
            dos.writeBoolean(this.checkbox_SplitClients.isSelected());
            dos.writeInt(((ClientOption)this.combobox_SelectClient.getSelectedItem()).getValue());
            FileUtils.save(CONFIGURATION_PATH, baos.toByteArray());
            dos.close();
            baos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void $$$setupUI$$$() {
        this.panel_Main = new JPanel();
        this.panel_Main.setLayout((LayoutManager)new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        this.panel_InnerMain = new JPanel();
        this.panel_InnerMain.setLayout(new BorderLayout(0, 0));
        this.panel_Main.add((Component)this.panel_InnerMain, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));
        this.panel_InnerBottom = new JPanel();
        this.panel_InnerBottom.setLayout((LayoutManager)new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_InnerMain.add((Component)this.panel_InnerBottom, "South");
        Spacer spacer1 = new Spacer();
        this.panel_InnerBottom.add((Component)spacer1, new GridConstraints(0, 0, 1, 1, 0, 1, 4, 1, null, null, null, 0, false));
        this.button_Exit = new JButton();
        this.button_Exit.setText("Tho\u00e1t");
        this.panel_InnerBottom.add((Component)this.button_Exit, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 0, null, null, null, 0, false));
        this.button_Login = new JButton();
        this.button_Login.setText("\u0110\u0103ng nh\u1eadp");
        this.panel_InnerBottom.add((Component)this.button_Login, new GridConstraints(0, 2, 1, 1, 0, 1, 3, 0, null, null, null, 0, false));
        this.panel_InnerCenter = new JPanel();
        this.panel_InnerCenter.setLayout((LayoutManager)new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        this.panel_InnerMain.add((Component)this.panel_InnerCenter, "Center");
        this.label_Username = new JLabel();
        this.label_Username.setText("T\u00e0i kho\u1ea3n:");
        CellConstraints cc = new CellConstraints();
        this.panel_InnerCenter.add((Component)this.label_Username, cc.xy(1, 1));
        this.label_Password = new JLabel();
        this.label_Password.setText("M\u1eadt kh\u1ea9u:");
        this.panel_InnerCenter.add((Component)this.label_Password, cc.xy(1, 3));
        this.field_Username = new JTextField();
        this.field_Username.setMinimumSize(new Dimension(49, 24));
        this.panel_InnerCenter.add((Component)this.field_Username, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        this.field_Password = new JPasswordField();
        this.field_Password.setMinimumSize(new Dimension(49, 24));
        this.panel_InnerCenter.add((Component)this.field_Password, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        JLabel label1 = new JLabel();
        label1.setText("Chia client:");
        this.panel_InnerCenter.add((Component)label1, cc.xy(1, 5));
        JPanel panel1 = new JPanel();
        panel1.setLayout((LayoutManager)new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        this.panel_InnerCenter.add((Component)panel1, cc.xy(3, 5));
        this.checkbox_SplitClients = new JCheckBox();
        this.checkbox_SplitClients.setText("");
        panel1.add((Component)this.checkbox_SplitClients, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        this.combobox_SelectClient = new JComboBox();
        panel1.add(this.combobox_SelectClient, new GridConstraints(0, 1, 1, 1, 8, 1, 5, 0, null, new Dimension(-1, 24), new Dimension(-1, 24), 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return this.panel_Main;
    }

    private static class ClientOption {
        private String title;
        private int value;

        public String toString() {
            return this.title;
        }

        public ClientOption(String title, int value) {
            this.title = title;
            this.value = value;
        }

        public ClientOption() {
        }

        public String getTitle() {
            return this.title;
        }

        public int getValue() {
            return this.value;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}

