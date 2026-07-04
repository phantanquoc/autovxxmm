/*
 * Decompiled with CFR 0.152.
 */
package core.service;

import core.model.Bot;
import core.service.LoginLimit;
import main.Application;
import service.SettingService;
import utils.Res;

public class AutoLogin
implements Runnable {
    private static int staggerSlot = 0;
    private static long lastPowerTime = 0L;
    private final Bot bot;
    public boolean onThread;
    public int countdownLogin;
    public long lastTimeCountdown;
    public boolean isLogin;
    public boolean isLoginSubmiting;
    private boolean power = true;
    private long timeLogin;

    public AutoLogin(Bot bot) {
        this.bot = bot;
        this.onThread = true;
        new Thread((Runnable)this, "AutoLogin [" + bot.getAccount() + "]").start();
    }

    @Override
    public void run() {
        while (this.onThread) {
            Res.sleep(1000L);
            boolean shouldDisconnect = this.shouldDissconnect();
            if (this.bot.getCurrentScreen() == 1) {
                this.isLoginSubmiting = false;
                if (shouldDisconnect) {
                    this.reconnect();
                }
            }
            if (shouldDisconnect) continue;
            if (!this.power) {
                this.isLoginSubmiting = false;
                if (!this.bot.getConnection().isConnected) continue;
                this.bot.getConnection().closeConnectionAsynchronous();
                continue;
            }
            if (this.countdownLogin > 0) {
                if (Res.t() - this.lastTimeCountdown <= 1000L) continue;
                --this.countdownLogin;
                this.lastTimeCountdown = Res.t();
                if (this.countdownLogin != 0) continue;
                this.login();
                continue;
            }
            if (this.timeLogin == 0L || Res.t() - this.timeLogin <= 60000L || this.bot.getCurrentScreen() != 0) continue;
            this.reconnect();
        }
    }

    private boolean shouldDissconnect() {
        return !this.bot.isEnable() || Application.systemInterrupt || this.bot.getRole() == 1 && !this.bot.getScreen().transferScreen().isOnTransfer();
    }

    public void reconnect() {
        this.startReconnect(Res.random(5, 10));
    }

    public void startReconnect(int second) {
        if (this.countdownLogin > 0) {
            return;
        }
        if (second < 5) {
            second = 5;
        }
        this.isLogin = false;
        this.bot.getConnection().cleanNetwork();
        this.bot.getConnection().bot.getWaitAction().notifyConnection();
        this.bot.removeAllVector(true);
        this.countdownLogin = second;
        this.isLoginSubmiting = false;
        this.lastTimeCountdown = Res.t();
        this.timeLogin = 0L;
    }

    private void login() {
        this.isLogin = true;
        this.timeLogin = Res.t();
        new Thread(() -> {
            if (LoginLimit.getInstance(this.bot.getServer().getId()).checkLimit(this.bot)) {
                this.isLoginSubmiting = true;
                this.bot.getConnection().cleanNetwork();
                this.bot.getConnection().openConnect();
                this.bot.getConnection().getService().login(this.bot.getAccount(), this.bot.getPassword(), this.bot.getServerType());
                this.bot.getWaitAction().waitConnection(30000L);
                if (this.bot.getPlayerName() != null) {
                    this.bot.getConnection().getService().selectCharToPlay(this.bot.getPlayerName());
                }
            } else {
                this.reconnect();
            }
        }).start();
    }

    public void power(boolean power) {
        this.power = power;
        if (power) {
            this.countdownLogin = AutoLogin.nextStaggerCountdown();
            this.lastTimeCountdown = Res.t();
        } else {
            this.countdownLogin = 0;
            this.lastTimeCountdown = 0L;
            this.isLogin = false;
            this.timeLogin = 0L;
        }
    }

    // Rải thời điểm login lúc khởi động: các bot tạo trong cùng đợt nhận
    // countdown 5, 5+gap, 5+2*gap... để server không thấy burst kết nối từ 1 IP.
    // Nếu >60s không có bot mới nào bật (đã qua đợt tạo hàng loạt) thì reset slot
    // => bot thêm lẻ lúc đang chạy vẫn login ngay sau 5s.
    private static synchronized int nextStaggerCountdown() {
        long now = Res.t();
        int gap = SettingService.getInstance().getLoginStagger();
        if (gap <= 0 || now - lastPowerTime > 60000L) {
            staggerSlot = 0;
        }
        lastPowerTime = now;
        int countdown = 5 + staggerSlot * gap;
        ++staggerSlot;
        return countdown;
    }
}

