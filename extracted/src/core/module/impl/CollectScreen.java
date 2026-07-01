/*
 * Decompiled with CFR 0.152.
 */
package core.module.impl;

import core.model.Bot;
import core.module.GameScreen;
import java.util.List;
import service.BotService;
import utils.Res;

public class CollectScreen
extends GameScreen {
    public static final int COIN_MAX = 2000000000;
    private static final long TIMEOUT_COLLECT = 60000L;
    /**
     * Per-task keep value populated by the collect-task poller before calling onCollect(true).
     * Replaces the old hardcoded VIP rule (coins - 10_000_000 for VIP, coins for normal).
     * All collect bots share the same keep value within a single CollectTask run.
     */
    public static long coinKeep = 0L;
    /**
     * Active task target bot IDs populated by the collect-task poller before calling onCollect(true).
     * When non-empty, CollectScreen will only collect from ORDER bots whose id is in this set.
     * When empty (no active task), no targets to gom → bot không tự ý gom.
     */
    public static java.util.Set<Integer> activeTargets = new java.util.HashSet<Integer>();
    private boolean onCollect;
    private Bot botCollect;
    private long timeStartCollect = 0L;

    public CollectScreen(Bot bot) {
        super(bot);
    }

    @Override
    protected void onAliveActivities() {
        boolean skip;
        if (!this.onCollect) {
            return;
        }
        boolean bl = skip = this.botCollect == null || !this.botCollect.isOnline() || !this.botCollect.isMapValid() || this.calculateCoinToCollect(this.botCollect.getTypeLuckyDraw(), this.botCollect.getMyChar().getCoin()) <= 0 || this.bot.getMyChar().getCoin() >= 2000000000 || this.botCollect.getTrade().getAsOrderTrade().getBotCollect() != null && this.botCollect.getTrade().getAsOrderTrade().getBotCollect() != this.bot || this.timeStartCollect > 0L && Res.t() - this.timeStartCollect >= 60000L;
        if (skip) {
            if (this.botCollect != null) {
                this.botCollect.getTrade().getAsOrderTrade().setBotCollect(null);
            }
            if (this.bot.getMyChar().getCoin() >= 2000000000) {
                return;
            }
            List<Bot> botOrders = BotService.getInstance().getBotOrders();
            Bot result = null;
            for (Bot botOrder : botOrders) {
                int coinCollect;
                if (!CollectScreen.activeTargets.isEmpty() && !CollectScreen.activeTargets.contains(botOrder.getId()) || botOrder == this.botCollect || botOrder.getServer().getId() != this.bot.getServer().getId() || !botOrder.isOnline() || !botOrder.isMapValid() || botOrder.getTrade().getAsOrderTrade().getBotCollect() != null || (coinCollect = this.calculateCoinToCollect(botOrder.getTypeLuckyDraw(), botOrder.getMyChar().getCoin())) <= 0 || coinCollect + this.bot.getMyChar().getCoin() > 2000000000) continue;
                result = botOrder;
                break;
            }
            if (result != null) {
                this.botCollect = result;
                this.botCollect.getTrade().getAsOrderTrade().setBotCollect(this.bot);
                this.timeStartCollect = Res.t();
            } else {
                this.botCollect = null;
                this.timeStartCollect = 0L;
                return;
            }
        }
        if (!this.compareMapAndZone(this.botCollect.getMapId(), this.botCollect.getZoneId())) {
            this.changeMapAndZone(this.botCollect.getMapId(), this.botCollect.getZoneId());
            return;
        }
        if (this.bot.getTrade().isShow()) {
            this.bot.getTrade().update();
        } else {
            this.bot.getTrade().findThenInvite(this.botCollect.getMyChar().getName());
        }
    }

    public void onCollect(boolean state) {
        this.onCollect = state;
        this.timeStartCollect = 0L;
        if (this.botCollect != null) {
            this.botCollect.getTrade().getAsOrderTrade().setBotCollect(null);
            this.botCollect = null;
        }
    }

    private int calculateCoinToCollect(int typeLucky, int coins) {
        // Previously: typeLucky == 0 ? coins - 10_000_000 : coins  (VIP rule)
        // Now: use the backend-supplied coinKeep value for all bot types.
        // typeLucky parameter is retained in the signature so call sites (onAliveActivities)
        // do not need to change, but it is no longer read here.
        return (int) Math.max(0L, (long) coins - CollectScreen.coinKeep);
    }

    @Override
    public final void updateParty() {
        if (this.botCollect == null) {
            if (!this.bot.getParty().isEmpty()) {
                this.bot.getConnection().getService().leaveParty();
                this.bot.debug("r\u1eddi nh\u00f3m v\u00ec kh\u00f4ng c\u00f3 bot gom xu");
            }
        } else {
            if (this.compareMapAndZone(this.botCollect.getMapId(), this.botCollect.getZoneId())) {
                if (!this.bot.getParty().isEmpty()) {
                    this.bot.getConnection().getService().leaveParty();
                    this.bot.debug("r\u1eddi nh\u00f3m v\u00ec \u0111\u00e3 c\u00f9ng map v\u1edbi bot gom xu");
                }
                return;
            }
            if (!this.sameParty(this.botCollect.getMyChar().getName())) {
                boolean invitePartyBotCollect = true;
                boolean outPartyBotCollect = false;
                if (!this.botCollect.getParty().isEmpty()) {
                    if (this.botCollect.getParty().size() < 6) {
                        this.bot.getConnection().getService().requestJoinParty(this.botCollect.getMyChar().getName());
                        this.bot.debug("g\u1eedi y\u00eau c\u1ea7u v\u00e0o nh\u00f3m v\u1edbi bot gom xu " + this.botCollect.getMyChar().getName());
                        Res.sleep(500L);
                        if (this.sameParty(this.botCollect.getMyChar().getName())) {
                            invitePartyBotCollect = false;
                            this.bot.debug("\u0111\u00e3 v\u00e0o chung nh\u00f3m v\u1edbi bot gom xu " + this.botCollect.getMyChar().getName());
                        } else {
                            outPartyBotCollect = true;
                            this.bot.debug("cho bot gom xu r\u1eddi nh\u00f3m (1)");
                        }
                    } else {
                        outPartyBotCollect = true;
                        this.bot.debug("cho bot gom xu r\u1eddi nh\u00f3m v\u00ec nh\u00f3m \u0111\u00e3 \u0111\u1ea1t t\u1ed1i \u0111a th\u00e0nh vi\u00ean");
                    }
                }
                if (outPartyBotCollect) {
                    this.botCollect.getConnection().getService().leaveParty();
                    this.bot.debug("th\u1ef1c hi\u1ec7n thao t\u00e1c r\u1eddi nh\u00f3m \u1edf bot gom xu");
                    Res.sleep(500L);
                }
                if (invitePartyBotCollect) {
                    boolean createNewParty = false;
                    if (!this.bot.getParty().isEmpty()) {
                        if (this.bot.getParty().size() < 6) {
                            this.bot.getConnection().getService().inviteIntoParty(this.botCollect.getMyChar().getName());
                            this.bot.debug("m\u1eddi bot gom xu " + this.botCollect.getMyChar().getName() + " v\u00e0o nh\u00f3m");
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
                        this.bot.getConnection().getService().inviteIntoParty(this.botCollect.getMyChar().getName());
                        this.bot.debug("\u0111\u00e3 g\u1eedi l\u1eddi m\u1eddi v\u00e0o nh\u00f3m \u0111\u1ebfn bot gom xu " + this.botCollect.getMyChar().getName());
                    }
                }
            }
        }
    }

    @Override
    public final String getStatusName() {
        if (!this.onCollect) {
            if (this.bot.getCurrentScreen() == 1) {
                return "S\u1eb5n s\u00e0ng";
            }
            if (this.bot.getAutoLogin().countdownLogin > 0) {
                return "\u0110\u0103ng nh\u1eadp l\u1ea1i sau " + this.bot.getAutoLogin().countdownLogin + "s";
            }
            if (this.bot.getAutoLogin().isLoginSubmiting) {
                return "\u0110ang \u0111\u0103ng nh\u1eadp";
            }
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
            if (this.botCollect == null) {
                return "Ch\u01b0a c\u00f3 bot \u0111\u1ec3 gom xu";
            }
            return "Gom xu bot: " + this.botCollect.getMyChar().getName();
        }
        return "";
    }

    public boolean isOnCollect() {
        return this.onCollect;
    }

    public Bot getBotCollect() {
        return this.botCollect;
    }
}

