/*
 * Decompiled with CFR 0.152.
 */
package datomic.btset;

public interface IBTSetBranch {
    public long count();

    public Object childAt(long var1);

    public Object upsert(long var1, Object var3);
}

