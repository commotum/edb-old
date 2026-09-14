/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Util;
import java.util.Comparator;

public final class stats$merge_splits$fn$reify__17924
implements Comparator,
IObj {
    final IPersistentMap __meta;

    public stats$merge_splits$fn$reify__17924(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public stats$merge_splits$fn$reify__17924() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new stats$merge_splits$fn$reify__17924(iPersistentMap);
    }

    public int compare(Object a, Object b) {
        Object object = a;
        a = null;
        Object object2 = b;
        b = null;
        return Util.compare((Object)object, (Object)object2);
    }
}

