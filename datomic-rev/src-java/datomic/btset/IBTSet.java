/*
 * Decompiled with CFR 0.152.
 */
package datomic.btset;

import datomic.iter.Iter;

public interface IBTSet {
    public Iter rseek();

    public Iter rseek(Object var1);
}

