/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import java.util.function.LongBinaryOperator;

public final class monitor$reify__518
implements LongBinaryOperator,
IObj {
    final IPersistentMap __meta;

    public monitor$reify__518(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public monitor$reify__518() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new monitor$reify__518(iPersistentMap);
    }

    public long applyAsLong(long a, long l) {
        return Numbers.min((long)a, (long)l);
    }
}

