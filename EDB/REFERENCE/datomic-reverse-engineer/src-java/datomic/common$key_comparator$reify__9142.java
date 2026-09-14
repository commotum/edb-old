/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import java.util.Comparator;

public final class common$key_comparator$reify__9142
implements Comparator,
IObj {
    final IPersistentMap __meta;
    Object key_fn;

    public common$key_comparator$reify__9142(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.key_fn = object;
    }

    public common$key_comparator$reify__9142(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new common$key_comparator$reify__9142(iPersistentMap, this.key_fn);
    }

    public int compare(Object o1, Object o2) {
        Object object = o1;
        o1 = null;
        Object object2 = o2;
        o2 = null;
        common$key_comparator$reify__9142 this_ = null;
        return ((Comparable)((IFn)this_.key_fn).invoke(object)).compareTo(((IFn)this_.key_fn).invoke(object2));
    }
}

