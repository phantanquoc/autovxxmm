/*
 * Decompiled with CFR 0.152.
 */
package core.service;

import java.util.HashMap;
import java.util.Map;
import lib.mVector;
import service.SettingService;
import utils.Res;

public class Spam {
    public static Map<Integer, Spam> spams = new HashMap<Integer, Spam>();
    private mVector vCharSpam = new mVector("vCharSpam");

    public static Spam getInstance(int serverId) {
        if (spams.get(serverId) == null) {
            spams.put(serverId, new Spam());
        }
        return spams.get(serverId);
    }

    public mVector vCharSpam() {
        return this.vCharSpam;
    }

    public boolean addSpam(String name) {
        CharSpam spam = this.getCharSpam(name);
        if (spam == null) {
            spam = new CharSpam(name);
            this.vCharSpam.addElement(spam);
        } else {
            ++spam.times;
        }
        if (spam.times >= SettingService.getInstance().getBlockSpamAfter()) {
            spam.time = Res.t();
            return true;
        }
        return false;
    }

    public CharSpam getCharSpam(String name) {
        for (int i = 0; i < this.vCharSpam.size(); ++i) {
            CharSpam spam = (CharSpam)this.vCharSpam.elementAt(i);
            if (spam == null || !spam.name.equals(name)) continue;
            return spam;
        }
        return null;
    }

    public void removeCharSpam(String name) {
        for (int i = 0; i < this.vCharSpam.size(); ++i) {
            CharSpam spam = (CharSpam)this.vCharSpam.elementAt(i);
            if (spam == null || !spam.name.equals(name)) continue;
            this.vCharSpam.removeElement(spam);
            return;
        }
    }

    public boolean isBlocked(String name) {
        for (int i = 0; i < this.vCharSpam.size(); ++i) {
            CharSpam spam = (CharSpam)this.vCharSpam.elementAt(i);
            if (spam == null || !spam.name.equals(name) || spam.times < SettingService.getInstance().getBlockSpamAfter()) continue;
            if (Res.t() - spam.time > (long)(SettingService.getInstance().getTimeBlockSpam() * 60) * 1000L) {
                this.vCharSpam.removeElement(spam);
                return false;
            }
            return true;
        }
        return false;
    }

    public static class CharSpam {
        public String name;
        public int times;
        public long time;

        public CharSpam(String name) {
            this.name = name;
            this.times = 1;
        }
    }
}

