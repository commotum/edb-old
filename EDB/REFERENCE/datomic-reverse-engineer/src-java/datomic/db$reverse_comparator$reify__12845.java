/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import java.util.Comparator;

public final class db$reverse_comparator$reify__12845
implements Comparator,
IObj {
    final IPersistentMap __meta;
    Object cmp;

    public db$reverse_comparator$reify__12845(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.cmp = object;
    }

    public db$reverse_comparator$reify__12845(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new db$reverse_comparator$reify__12845(iPersistentMap, this.cmp);
    }

    public int compare(Object x, Object y) {
        Object object = y;
        y = null;
        Object object2 = x;
        x = null;
        db$reverse_comparator$reify__12845 this_ = null;
        return ((Comparator)this_.cmp).compare(object, object2);
    }
}

