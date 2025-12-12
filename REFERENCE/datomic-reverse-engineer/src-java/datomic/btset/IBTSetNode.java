/*
 * Decompiled with CFR 0.152.
 */
package datomic.btset;

import datomic.btset.IBTSetIterLink;
import datomic.iter.Iter;

public interface IBTSetNode {
    public Iter seek(IBTSetIterLink var1);

    public Iter seek(Object var1, IBTSetIterLink var2);

    public Iter rseek(IBTSetIterLink var1);

    public Object conjoin(Object var1);

    public long compare(Object var1, Object var2);
}

