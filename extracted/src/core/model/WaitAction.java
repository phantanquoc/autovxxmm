/*
 * Decompiled with CFR 0.152.
 */
package core.model;

public class WaitAction {
    public boolean isWaitPick = false;
    public boolean isWaitRegion = false;
    public boolean isWaitBuy = false;
    public boolean isWaitMa = false;
    public boolean isWaitMap = false;
    public boolean isWaitConn = false;
    public boolean isWaitPickItem = false;
    public boolean isWaitLuckyDraw = false;
    public boolean isWaitInputParty = false;
    public boolean isWaitDie = false;
    public boolean isWaitSaveMapReturnTown = false;
    private Object pick = new Object();
    private Object region = new Object();
    private Object buy = new Object();
    private Object ma = new Object();
    private Object map = new Object();
    private Object connection = new Object();
    private Object inputParty = new Object();
    private Object die = new Object();
    private Object saveMapReturnTown = new Object();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitSaveMapReturnTown() {
        Object object = this.saveMapReturnTown;
        synchronized (object) {
            try {
                this.isWaitSaveMapReturnTown = true;
                this.saveMapReturnTown.wait(5000L);
                this.isWaitSaveMapReturnTown = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifySaveMapReturnTown() {
        Object object = this.saveMapReturnTown;
        synchronized (object) {
            this.saveMapReturnTown.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitDie() {
        Object object = this.die;
        synchronized (object) {
            try {
                this.isWaitDie = true;
                this.die.wait(5000L);
                this.isWaitDie = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyDie() {
        Object object = this.die;
        synchronized (object) {
            this.die.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitInputParty() {
        Object object = this.inputParty;
        synchronized (object) {
            try {
                this.isWaitInputParty = true;
                this.inputParty.wait(5000L);
                this.isWaitInputParty = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyInputParty() {
        Object object = this.inputParty;
        synchronized (object) {
            try {
                this.isWaitInputParty = false;
                this.inputParty.notifyAll();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitConnection(long time) {
        Object object = this.connection;
        synchronized (object) {
            try {
                this.isWaitConn = true;
                this.connection.wait(time);
                this.isWaitConn = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyConnection() {
        Object object = this.connection;
        synchronized (object) {
            this.isWaitConn = false;
            this.connection.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitMap() {
        Object object = this.map;
        synchronized (object) {
            try {
                this.isWaitMap = true;
                this.map.wait(10000L);
                this.isWaitMap = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyWaitMap() {
        Object object = this.map;
        synchronized (object) {
            this.isWaitMap = false;
            this.map.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitPick() {
        Object object = this.pick;
        synchronized (object) {
            try {
                this.isWaitPick = true;
                this.pick.wait(2000L);
                this.isWaitPick = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyWaitPick() {
        Object object = this.pick;
        synchronized (object) {
            this.isWaitPick = false;
            this.pick.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitRegion() {
        Object object = this.region;
        synchronized (object) {
            try {
                this.isWaitRegion = true;
                this.region.wait(10000L);
                this.isWaitRegion = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyWaitRegion() {
        Object object = this.region;
        synchronized (object) {
            this.isWaitRegion = false;
            this.region.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitBuy() {
        Object object = this.buy;
        synchronized (object) {
            try {
                this.isWaitBuy = true;
                this.buy.wait(5000L);
                this.isWaitBuy = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyWaitBuy() {
        Object object = this.buy;
        synchronized (object) {
            this.isWaitBuy = false;
            this.buy.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitMa() {
        Object object = this.ma;
        synchronized (object) {
            try {
                this.isWaitMa = true;
                this.ma.wait(30000L);
                this.isWaitMa = false;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyMa() {
        Object object = this.ma;
        synchronized (object) {
            this.isWaitMa = false;
            this.ma.notifyAll();
        }
    }
}

