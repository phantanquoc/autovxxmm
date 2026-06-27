/*
 * Decompiled with CFR 0.152.
 */
package utils;

public class TextUtils {
    private static final String COMMENT = "**";

    public static boolean isComment(String st) {
        return st.startsWith(COMMENT);
    }

    public static int getInteger(String st, String key) {
        return Integer.parseInt(TextUtils.getText(st, key));
    }

    public static String getText(String st, String key) {
        if (!st.contains("[" + key + "]") || !st.contains("[/" + key + "]")) {
            return null;
        }
        String[] m = new String[]{"[" + key + "]", "[/" + key + "]"};
        String rt = st.substring(st.indexOf(m[0]) + m[0].length(), st.indexOf(m[1]));
        return !rt.isEmpty() ? rt : null;
    }

    public static String[] getArrayString(String st, String key) {
        String text = TextUtils.getText(st, key);
        if (text.contains(";")) {
            return text.split(";");
        }
        return null;
    }
}

