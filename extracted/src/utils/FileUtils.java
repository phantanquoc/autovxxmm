/*
 * Decompiled with CFR 0.152.
 */
package utils;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import main.Application;

public class FileUtils {
    public static String readAsString(String path) {
        try {
            FileInputStream is = new FileInputStream(path);
            byte[] bs = new byte[((InputStream)is).available()];
            ((InputStream)is).read(bs);
            ((InputStream)is).close();
            return new String(bs, "UTF-8");
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long readAsLong(String name) {
        try {
            return Long.parseLong(FileUtils.readString(name));
        }
        catch (Exception exception) {
            return -1L;
        }
    }

    public static long readByte(String name) {
        try {
            return Byte.parseByte(FileUtils.readString(name));
        }
        catch (Exception exception) {
            return -1L;
        }
    }

    public static int readInt(String name) {
        try {
            return Integer.parseInt(FileUtils.readString(name));
        }
        catch (Exception exception) {
            return -1;
        }
    }

    public static String readString(String name) {
        try {
            return new String(FileUtils.read(name), "UTF-8");
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static byte[] read(String path) {
        byte[] data = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                data = new byte[((InputStream)is).available()];
                ((InputStream)is).read(data);
                ((InputStream)is).close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return data;
    }

    public static void saveString(String name, String data, boolean isAppend) throws Exception {
        File file;
        if (isAppend && (file = new File(Application.DIR + name)).exists()) {
            FileInputStream is = new FileInputStream(file);
            byte[] bs = new byte[((InputStream)is).available()];
            ((InputStream)is).read(bs);
            data = data + new String(bs, "UTF-8");
            ((InputStream)is).close();
        }
        FileUtils.save(name, data.getBytes("UTF-8"));
    }

    public static void save(String path, byte[] data) throws Exception {
        File file = new File(path.substring(0, path.lastIndexOf("/")));
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!(file = new File(path)).exists()) {
            file.createNewFile();
        }
        FileOutputStream os = new FileOutputStream(file);
        ((OutputStream)os).write(data);
        os.flush();
        ((OutputStream)os).close();
    }

    public static boolean delete(String name) throws Exception {
        File file = new File(Application.DIR + name);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    public static String chooseFile(FileFilter filter) {
        return FileUtils.chooseFile(null, filter);
    }

    public static String chooseFile(Component parent, FileFilter filter) {
        return FileUtils.chooseFile(null, parent, filter);
    }

    public static String chooseFile(File curFolder, Component parent, FileFilter filter) {
        if (curFolder == null) {
            try {
                curFolder = new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        JFileChooser chooser = new JFileChooser(curFolder);
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(false);
        return chooser.showOpenDialog(parent) == 0 ? chooser.getSelectedFile().getAbsolutePath() : null;
    }
}

