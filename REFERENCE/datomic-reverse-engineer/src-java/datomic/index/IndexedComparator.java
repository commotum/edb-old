/*
 * Decompiled with CFR 0.152.
 */
package datomic.index;

import datomic.db.Datum;
import datomic.index.TransposedData;

public interface IndexedComparator {
    public long compare(Datum var1, TransposedData var2, long var3);
}

