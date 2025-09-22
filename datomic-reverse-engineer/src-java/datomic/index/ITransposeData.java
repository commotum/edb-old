/*
 * Decompiled with CFR 0.152.
 */
package datomic.index;

public interface ITransposeData {
    public boolean isAssertion(int var1);

    public long getE(int var1);

    public int getA(int var1);

    public Object getV(int var1);

    public long getT(int var1);

    public long getLongV(int var1);

    public double getDoubleV(int var1);

    public int getIntV(int var1);

    public float getFloatV(int var1);

    public boolean getBooleanV(int var1);

    public Object getEs();

    public Object getAs();
}

