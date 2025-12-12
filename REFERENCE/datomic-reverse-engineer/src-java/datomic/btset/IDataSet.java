/*
 * Decompiled with CFR 0.152.
 */
package datomic.btset;

import datomic.iter.Iter;

public interface IDataSet {
    public long longCount();

    public Iter seek();

    public Iter seek(Object var1);

    public Iter seekLast();
}

