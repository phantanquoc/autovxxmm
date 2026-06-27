/*
 * Decompiled with CFR 0.152.
 */
package lib;

import java.util.Vector;

public class mVector
extends Vector {
    public mVector(String label) {
    }

    @Override
    public final synchronized void removeAllElements() {
        super.removeAllElements();
    }

    @Override
    public final synchronized boolean removeElement(Object obj) {
        return super.removeElement(obj);
    }

    @Override
    public final synchronized void removeElementAt(int index) {
        super.removeElementAt(index);
    }

    public final synchronized void addElement(Object obj) {
        super.addElement(obj);
    }

    public final synchronized void insertElementAt(Object obj, int index) {
        super.insertElementAt(obj, index);
    }
}

