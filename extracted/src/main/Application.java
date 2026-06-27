/*
 * Decompiled with CFR 0.152.
 */
package main;

import constants.API;
import java.awt.Component;
import java.io.InputStream;
import java.util.logging.LogManager;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import service.BotService;
import ui.LoginUI;

public class Application {
    public static String DIR = "./";
    public static boolean systemInterrupt;
    public static boolean isSplitClient;
    public static int client;

    public static void main(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (!args[i].equals("--local")) continue;
            API.url = "http://localhost:8009";
            break;
        }
        Application.loadConfiguration();
        SwingUtilities.invokeLater(() -> LoginUI.getInstance().setVisible(true));
        Thread shutdownHook = new Thread(() -> {
            systemInterrupt = true;
            BotService.getInstance().getUpdaterService().clientExit();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static void loadConfiguration() {
        InputStream stream = Application.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        }
        catch (Exception e) {
            e.printStackTrace();
            Application.error("C\u00f3 l\u1ed7i x\u1ea3y ra khi kh\u1edfi \u0111\u1ed9ng \u1ee9ng d\u1ee5ng!");
        }
    }

    public static void info(String message) {
        Application.info(null, message);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Th\u00f4ng b\u00e1o", 1);
    }

    public static void alert(String message) {
        Application.alert(null, message);
    }

    public static void alert(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Th\u00f4ng b\u00e1o", 2);
    }

    public static void warning(String message) {
        Application.warning(null, message);
    }

    public static void warning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Th\u00f4ng b\u00e1o", 0);
    }

    public static boolean confirm(String message) {
        return Application.confirm(null, message);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "X\u00e1c nh\u1eadn", 0) == 0;
    }

    public static void error(String message) {
        Application.error(null, "L\u1ed7i", message);
    }

    public static void error(Component parent, String message) {
        Application.error(parent, "L\u1ed7i", message);
    }

    public static void error(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, 0);
    }
}

