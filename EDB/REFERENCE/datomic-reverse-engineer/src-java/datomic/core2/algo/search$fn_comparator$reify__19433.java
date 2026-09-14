/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic.core2.algo;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import java.util.Comparator;

public final class search$fn_comparator$reify__19433
implements Comparator,
IObj {
    final IPersistentMap __meta;
    Object f;

    public search$fn_comparator$reify__19433(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.f = object;
    }

    public search$fn_comparator$reify__19433(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new search$fn_comparator$reify__19433(iPersistentMap, this.f);
    }

    public int compare(Object o1, Object o2) {
        Object object = o1;
        o1 = null;
        Object object2 = o2;
        o2 = null;
        search$fn_comparator$reify__19433 this_ = null;
        return ((Comparable)((IFn)this_.f).invoke(object)).compareTo(((IFn)this_.f).invoke(object2));
    }
}

