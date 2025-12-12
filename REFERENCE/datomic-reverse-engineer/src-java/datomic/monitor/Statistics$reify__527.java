/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic.monitor;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

public final class Statistics$reify__527
implements Function,
IObj {
    final IPersistentMap __meta;

    public Statistics$reify__527(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public Statistics$reify__527() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new Statistics$reify__527(iPersistentMap);
    }

    public Object apply(Object metric) {
        return new LongAdder();
    }
}

