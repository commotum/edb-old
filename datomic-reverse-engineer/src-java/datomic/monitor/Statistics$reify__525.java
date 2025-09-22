/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.monitor;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.Function;
import java.util.function.LongBinaryOperator;

public final class Statistics$reify__525
implements Function,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"max*");

    public Statistics$reify__525(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public Statistics$reify__525() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new Statistics$reify__525(iPersistentMap);
    }

    public Object apply(Object metric) {
        return new LongAccumulator((LongBinaryOperator)const__0.getRawRoot(), Long.MIN_VALUE);
    }
}

