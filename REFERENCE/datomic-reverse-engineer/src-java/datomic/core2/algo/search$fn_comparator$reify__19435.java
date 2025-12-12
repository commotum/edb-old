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

public final class search$fn_comparator$reify__19435
implements Comparator,
IObj {
    final IPersistentMap __meta;
    Object f;
    Object comp;

    public search$fn_comparator$reify__19435(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.f = object;
        this.comp = object2;
    }

    public search$fn_comparator$reify__19435(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new search$fn_comparator$reify__19435(iPersistentMap, this.f, this.comp);
    }

    public int compare(Object o1, Object o2) {
        Object object = o1;
        o1 = null;
        Object object2 = o2;
        o2 = null;
        search$fn_comparator$reify__19435 this_ = null;
        return ((Comparator)this_.comp).compare(((IFn)this_.f).invoke(object), ((IFn)this_.f).invoke(object2));
    }
}

