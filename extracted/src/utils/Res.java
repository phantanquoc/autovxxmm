/*
 * Decompiled with CFR 0.152.
 */
package utils;

import core.cache.DataStream;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import main.Application;

public class Res {
    private static Random rd = new Random();
    public static String url;

    public static String cTime() {
        return Res.addZero(Res._HOUR()) + ":" + Res.addZero(Res._MIN()) + ":" + Res.addZero(Res._SEC());
    }

    public static byte _HOUR() {
        Calendar cal = Calendar.getInstance();
        return (byte)cal.get(11);
    }

    public static byte _MIN() {
        Calendar cal = Calendar.getInstance();
        return (byte)cal.get(12);
    }

    public static byte _SEC() {
        Calendar cal = Calendar.getInstance();
        return (byte)cal.get(13);
    }

    public static String currentDayTime() {
        Calendar cal = Calendar.getInstance();
        return Res.addZero(cal.get(5)) + "-" + Res.addZero(cal.get(2) + 1) + "-" + cal.get(1) + " " + Res.cTime();
    }

    public static int random(int a, int b) {
        return rd.nextInt(b - a) + a;
    }

    public static int random(int a) {
        return rd.nextInt(a);
    }

    public static int abs(int a) {
        return a < 0 ? -a : a;
    }

    public static int remainSecond(int maxSecond, long time) {
        int second = (int)((long)maxSecond * 1000L - (Res.t() - time)) / 1000;
        return second < 0 ? 0 : second;
    }

    public static String randomNumberlist() {
        String number = "";
        for (int i = 0; i < 12; ++i) {
            int temp = Res.random(0, 9);
            String numbertemp = Integer.toString(temp);
            number = number + numbertemp;
        }
        return number;
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public static String moneyFormat(String number) {
        String value = "";
        String value1 = "";
        if (number.equals("")) {
            return value;
        }
        if (number.charAt(0) == '-') {
            value1 = "-";
            number = number.substring(1);
        }
        for (int i = number.length() - 1; i >= 0; --i) {
            value = (number.length() - 1 - i) % 3 == 0 && number.length() - 1 - i > 0 ? number.charAt(i) + "," + value : number.charAt(i) + value;
        }
        return value1 + value;
    }

    public static String moneyFormat(long number) {
        return Res.moneyFormat(String.valueOf(number));
    }

    public static long t() {
        return System.currentTimeMillis();
    }

    public static void sleep(long l) {
        try {
            Thread.sleep(l);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static Date date() {
        return new Date();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Image createImageIcon(String path) {
        try (InputStream is = Application.class.getResourceAsStream(path);){
            byte[] ba = new byte[is.available()];
            is.read(ba);
            ImageIcon icon = new ImageIcon(ba);
            Image image = icon.getImage();
            return image;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String readFileText(String path) {
        try {
            InputStream is = Application.class.getResource(path).openStream();
            byte[] bs = new byte[is.available()];
            is.read(bs);
            return new String(bs, "UTF-8");
        }
        catch (Exception e) {
            e.printStackTrace();
            Res.printError(e);
            return null;
        }
    }

    public static byte[] readFile(String name) {
        byte[] data = null;
        try {
            File file = new File(Application.DIR + name);
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

    public static void saveFile(String name, String data, boolean isAppend) throws Exception {
        File file;
        if (isAppend && (file = new File(Application.DIR + name)).exists()) {
            FileInputStream is = new FileInputStream(file);
            byte[] bs = new byte[((InputStream)is).available()];
            ((InputStream)is).read(bs);
            data = data + new String(bs, "UTF-8");
            ((InputStream)is).close();
        }
        Res.saveFile(name, data.getBytes("UTF-8"));
    }

    public static void saveFile(String name, byte[] data) throws Exception {
        File file = new File(Application.DIR + name.substring(0, name.lastIndexOf("/")));
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!(file = new File(Application.DIR + name)).exists()) {
            file.createNewFile();
        }
        FileOutputStream os = new FileOutputStream(file);
        ((OutputStream)os).write(data);
        os.flush();
        ((OutputStream)os).close();
    }

    public static boolean deleteFile(String name) throws Exception {
        File file = new File(Application.DIR + name);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    public static long getTime(String st, String head, String foot) {
        if (st.indexOf(head) != -1 && st.indexOf(foot) != -1) {
            String sub = st.substring(head.length(), st.indexOf(foot));
            return Long.parseLong(sub);
        }
        return -1L;
    }

    public static void print(String string) {
        System.out.println(string);
    }

    public static void printError(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String log = sw.toString();
        Calendar c = Calendar.getInstance();
        int year = c.get(1);
        int month = c.get(2);
        int day = c.get(5);
        int hour = c.get(11);
        int minute = c.get(12);
        int second = c.get(13);
        String fileName = "log-" + day + "-" + month + "-" + year + ".txt";
        String lineAdd = "\r\n[" + hour + ":" + minute + ":" + second + "]\r\n{\r\n" + log + "\r\n}";
        try {
            Res.saveFile(fileName, lineAdd, true);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static String getTimeNow() {
        Calendar c = Calendar.getInstance();
        String hour = Res.addZero(c.get(11));
        String minute = Res.addZero(c.get(12));
        String second = Res.addZero(c.get(13));
        return "[" + hour + ":" + minute + ":" + second + "] ";
    }

    private static String Replace(String src, String find, String edit) {
        StringBuffer sb = new StringBuffer();
        int index = src.indexOf(find);
        int begin = 0;
        int i = find.length();
        while (index != -1) {
            sb.append(src.substring(begin, index)).append(edit);
            begin = index + i;
            index = src.indexOf(find, begin);
        }
        sb.append(src.substring(begin, src.length()));
        return sb.toString();
    }

    public static String[] split(String st, String key) {
        if (st.trim().equals("")) {
            return new String[0];
        }
        return st.split(key);
    }

    public static String getTime(int seconds) {
        int minutes = 0;
        if (seconds > 60) {
            minutes = seconds / 60;
            seconds %= 60;
        }
        int hours = 0;
        if (minutes > 60) {
            hours = minutes / 60;
            minutes %= 60;
        }
        int days = 0;
        if (hours > 24) {
            days = hours / 24;
            hours %= 24;
        }
        String rt = "";
        if (days > 0) {
            rt = rt + days + "d";
            rt = rt + hours + "h";
        } else if (hours > 0) {
            rt = rt + hours + "h";
            rt = rt + minutes + "m";
        } else {
            rt = minutes > 9 ? rt + minutes + "m" : rt + "0" + minutes + "m";
            rt = seconds > 9 ? rt + seconds + "s" : rt + "0" + seconds + "s";
        }
        return rt;
    }

    public static long calculateSecond(long milliseconds) {
        return (Res.t() - milliseconds) / 1000L;
    }

    public static String toTime(long seconds) {
        return Res.toTime((int)seconds);
    }

    public static String toTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds -= hours * 3600) / 60;
        return Res.addZero(hours) + ":" + Res.addZero(minutes) + ":" + Res.addZero(seconds -= minutes * 60);
    }

    public static String b2kb(int size) {
        int div = size / 1024;
        int mod = size % 1024;
        return div + "." + mod + "Kb";
    }

    public static long[] getLevelExp(long exp) {
        long expRemain = exp;
        int i = 0;
        for (i = 0; i < DataStream.arrExp.length && expRemain >= DataStream.arrExp[i]; expRemain -= DataStream.arrExp[i], ++i) {
        }
        return new long[]{i, expRemain};
    }

    public static long getMaxExp(int level) {
        long num = 0L;
        for (int i = 0; i <= level; ++i) {
            num += DataStream.arrExp[i];
        }
        return num;
    }

    public static String addZero(int value) {
        if (value < 10) {
            return "0" + value;
        }
        return "" + value;
    }

    public static String replaceParam(String text, int param) {
        return text.replace("#", String.valueOf(param));
    }

    public static int filterNumber(String text) {
        StringBuffer num = new StringBuffer();
        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) < '0' || text.charAt(i) > '9') continue;
            num.append(text.charAt(i));
        }
        if (!num.toString().isEmpty()) {
            return Integer.parseInt(num.toString());
        }
        return -1;
    }

    public static String getTimeCountDown(long timeStart, int secondCount) {
        String info = "";
        long t = (timeStart + (long)(secondCount * 1000) - Res.t()) / 1000L;
        if (t <= 0L) {
            return "";
        }
        long phut = t / 60L;
        long giay = t;
        info = phut > 0L ? (phut < 10L ? (t % 60L >= 0L && t % 60L < 10L ? "0" + phut + ":0" + t % 60L : "0" + phut + ":" + t % 60L) : (t % 60L >= 0L && t % 60L < 10L ? phut + ":0" + t % 60L : phut + ":" + t % 60L)) : (giay < 10L ? "0" + giay + "s" : giay + "s");
        return info;
    }

    public static int getNumber(String num) {
        try {
            return Integer.parseInt(num);
        }
        catch (Exception exception) {
            return -1;
        }
    }

    public static int cNum(String st, String lc) {
        if (!st.startsWith(lc)) {
            return -1;
        }
        try {
            st = st.substring(lc.length());
            return Integer.parseInt(st);
        }
        catch (Exception exception) {
            return -1;
        }
    }

    public static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidUsername(String name) {
        String regex = "^[A-Za-z0-9]{5,15}$";
        Pattern p = Pattern.compile(regex);
        if (name == null) {
            return false;
        }
        Matcher m = p.matcher(name);
        return m.matches();
    }

    public static String generateString(int lent) {
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();
        String generatedString = random.ints(leftLimit, rightLimit + 1).filter(i -> !(i > 57 && i < 65 || i > 90 && i < 97)).limit(lent).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        return generatedString;
    }
}

