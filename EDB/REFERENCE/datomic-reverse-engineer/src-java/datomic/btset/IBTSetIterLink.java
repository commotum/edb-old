/*
 * Decompiled with CFR 0.152.
 */
package datomic.btset;

public interface IBTSetIterLink {
    public long offset();

    public void incOffset();

    public void decOffset();
}

