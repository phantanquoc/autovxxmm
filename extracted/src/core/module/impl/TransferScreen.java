/*
 * Decompiled with CFR 0.152.
 */
package core.module.impl;

import core.model.Bot;
import core.module.GameScreen;
import java.util.List;
import service.BotService;
import utils.Res;

public class TransferScreen
extends GameScreen {
    private boolean onTransfer;
    private Bot botTransfer;
    private long timeStartTransfer = 0L;

    public TransferScreen(Bot bot) {
        super(bot);
    }

    @Override
    protected void onAliveActivities() {
        if (!this.onTransfer || this.bot.getMyChar().getCoin() <= 0) {
            return;
        }
        if (this.isSkipTransfer()) {
            Bot nextBotTransfer;
            if (this.botTransfer != null) {
                this.botTransfer.getTrade().getAsOrderTrade().setBotTransfer(null);
            }
            if ((nextBotTransfer = this.getBotTransfer(this.botTransfer, this.bot.getServer().getId())) != null) {
                this.botTransfer = nextBotTransfer;
                this.botTransfer.getTrade().getAsOrderTrade().setBotTransfer(this.bot);
                this.timeStartTransfer = Res.t();
            } else {
                this.botTransfer = null;
                this.timeStartTransfer = 0L;
                return;
            }
        }
        if (!this.compareMapAndZone(this.botTransfer.getMapId(), this.botTransfer.getZoneId())) {
            this.changeMapAndZone(this.botTransfer.getMapId(), this.botTransfer.getZoneId());
            return;
        }
        if (this.bot.getTrade().isShow()) {
            this.bot.getTrade().update();
        } else {
            this.bot.getTrade().findThenInvite(this.botTransfer.getMyChar().getName());
        }
    }

    private Bot getBotTransfer(Bot ignore, int serverId) {
        List<Bot> botOrders = BotService.getInstance().getBotOrders();
        Bot result = null;
        for (int i = 0; i < botOrders.size(); ++i) {
            Bot bot = botOrders.get(i);
            if (bot == ignore || bot.getServer().getId() != serverId || !bot.isOnline() || !bot.isMapValid() || bot.getTrade().getAsOrderTrade().getBotTransfer() != null || this.isEnoughCoin(bot)) continue;
            result = bot;
            break;
        }
        return result;
    }

    private boolean isSkipTransfer() {
        return this.botTransfer == null || !this.botTransfer.isOnline() || !this.botTransfer.isMapValid() || this.isEnoughCoin(this.botTransfer) || this.botTransfer.getTrade().getAsOrderTrade().getBotTransfer() != null && this.botTransfer.getTrade().getAsOrderTrade().getBotTransfer() != this.bot || this.timeStartTransfer > 0L && Res.t() - this.timeStartTransfer >= 60000L;
    }

    private boolean isEnoughCoin(Bot bot) {
        return bot.getTypeLuckyDraw() == 0 ? bot.getMyChar().getCoin() >= 10000000 : bot.getMyChar().getCoin() >= 100000;
    }

    public void onTransfer(boolean state) {
        this.onTransfer = state;
        this.timeStartTransfer = 0L;
        if (this.botTransfer != null) {
            this.botTransfer.getTrade().getAsOrderTrade().setBotTransfer(null);
            this.botTransfer = null;
        }
    }

    @Override
    public final void updateParty() {
        if (this.botTransfer == null) {
            if (!this.bot.getParty().isEmpty()) {
                this.bot.getConnection().getService().leaveParty();
                this.bot.debug("r\u1eddi nh\u00f3m v\u00ec kh\u00f4ng c\u00f3 bot b\u01a1m xu");
            }
        } else {
            if (this.compareMapAndZone(this.botTransfer.getMapId(), this.botTransfer.getZoneId())) {
                if (!this.bot.getParty().isEmpty()) {
                    this.bot.getConnection().getService().leaveParty();
                    this.bot.debug("r\u1eddi nh\u00f3m v\u00ec \u0111\u00e3 c\u00f9ng map v\u1edbi bot b\u01a1m xu");
                }
                return;
            }
            if (!this.sameParty(this.botTransfer.getMyChar().getName())) {
                boolean invitePartybotTransfer = true;
                boolean outPartybotTransfer = false;
                if (!this.botTransfer.getParty().isEmpty()) {
                    if (this.botTransfer.getParty().size() < 6) {
                        this.bot.getConnection().getService().requestJoinParty(this.botTransfer.getMyChar().getName());
                        this.bot.debug("g\u1eedi y\u00eau c\u1ea7u v\u00e0o nh\u00f3m v\u1edbi bot gom xu " + this.botTransfer.getMyChar().getName());
                        Res.sleep(500L);
                        if (this.sameParty(this.botTransfer.getMyChar().getName())) {
                            invitePartybotTransfer = false;
                            this.bot.debug("\u0111\u00e3 v\u00e0o chung nh\u00f3m v\u1edbi bot gom xu " + this.botTransfer.getMyChar().getName());
                        } else {
                            outPartybotTransfer = true;
                            this.bot.debug("cho bot gom xu r\u1eddi nh\u00f3m (1)");
                        }
                    } else {
                        outPartybotTransfer = true;
                        this.bot.debug("cho bot gom xu r\u1eddi nh\u00f3m v\u00ec nh\u00f3m \u0111\u00e3 \u0111\u1ea1t t\u1ed1i \u0111a th\u00e0nh vi\u00ean");
                    }
                }
                if (outPartybotTransfer) {
                    this.botTransfer.getConnection().getService().leaveParty();
                    this.bot.debug("th\u1ef1c hi\u1ec7n thao t\u00e1c r\u1eddi nh\u00f3m \u1edf bot gom xu");
                    Res.sleep(500L);
                }
                if (invitePartybotTransfer) {
                    boolean createNewParty = false;
                    if (!this.bot.getParty().isEmpty()) {
                        if (this.bot.getParty().size() < 6) {
                            this.bot.getConnection().getService().inviteIntoParty(this.botTransfer.getMyChar().getName());
                            this.bot.debug("m\u1eddi bot gom xu " + this.botTransfer.getMyChar().getName() + " v\u00e0o nh\u00f3m");
                            Res.sleep(500L);
                        } else {
                            createNewParty = true;
                            this.bot.getConnection().getService().leaveParty();
                            this.bot.debug("t\u1ea1o nh\u00f3m m\u1edbi (1)");
                            Res.sleep(500L);
                        }
                    } else {
                        createNewParty = true;
                        this.bot.debug("t\u1ea1o nh\u00f3m m\u1edbi (2)");
                    }
                    if (createNewParty) {
                        this.bot.getConnection().getService().createParty();
                        this.bot.debug("th\u1ef1c hi\u1ec7n thao t\u00e1c t\u1ea1o nh\u00f3m m\u1edbi");
                        Res.sleep(500L);
                        this.bot.getConnection().getService().inviteIntoParty(this.botTransfer.getMyChar().getName());
                        this.bot.debug("\u0111\u00e3 g\u1eedi l\u1eddi m\u1eddi v\u00e0o nh\u00f3m \u0111\u1ebfn bot gom xu " + this.botTransfer.getMyChar().getName());
                    }
                }
            }
        }
    }

    @Override
    public final String getStatusName() {
        if (!this.onTransfer) {
            return "\u0110ang ch\u1edd";
        }
        if (this.bot.getCurrentScreen() == 0) {
            if (this.bot.getAutoLogin().countdownLogin > 0) {
                return "\u0110\u0103ng nh\u1eadp l\u1ea1i sau " + this.bot.getAutoLogin().countdownLogin + "s";
            }
            if (this.bot.getAutoLogin().isLoginSubmiting) {
                return "\u0110ang \u0111\u0103ng nh\u1eadp";
            }
        } else {
            if (this.botTransfer == null) {
                return "Ch\u01b0a c\u00f3 bot \u0111\u1ec3 b\u01a1m xu";
            }
            return "B\u01a1m xu cho bot: " + this.botTransfer.getMyChar().getName();
        }
        return "";
    }

    public boolean isOnTransfer() {
        return this.onTransfer;
    }

    public Bot getBotTransfer() {
        return this.botTransfer;
    }
}

