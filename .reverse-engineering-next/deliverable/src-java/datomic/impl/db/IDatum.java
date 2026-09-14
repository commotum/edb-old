/*
 * Decompiled with CFR 0.152.
 */
package datomic.impl.db;

public interface IDatum {
    public boolean isAssertion();

    public long getE();

    public int getA();

    public long getT();

    public long getTx();

    public int getP();

    public Object getV();

    public long getLongV();

    public double getDoubleV();

    public int getIntV();

    public float getFloatV();

    public boolean getBooleanV();
}

