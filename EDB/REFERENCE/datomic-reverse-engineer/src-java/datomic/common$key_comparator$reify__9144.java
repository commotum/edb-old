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

public final class common$key_comparator$reify__9144
implements Comparator,
IObj {
    final IPersistentMap __meta;
    Object comp;
    Object key_fn;

    public common$key_comparator$reify__9144(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.comp = object;
        this.key_fn = object2;
    }

    public common$key_comparator$reify__9144(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new common$key_comparator$reify__9144(iPersistentMap, this.comp, this.key_fn);
    }

    public int compare(Object o1, Object o2) {
        Object object = o1;
        o1 = null;
        Object object2 = o2;
        o2 = null;
        common$key_comparator$reify__9144 this_ = null;
        return ((Comparator)this_.comp).compare(((IFn)this_.key_fn).invoke(object), ((IFn)this_.key_fn).invoke(object2));
    }
}

