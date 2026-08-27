/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.iter.Iter;

public final class db$filter_retractions$reify__12652
implements Iter,
IObj {
    final IPersistentMap __meta;
    Object iter_atom;
    Object next_skip;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"iget");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"swap!");

    public db$filter_retractions$reify__12652(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.iter_atom = object;
        this.next_skip = object2;
    }

    public db$filter_retractions$reify__12652(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new db$filter_retractions$reify__12652(iPersistentMap, this.iter_atom, this.next_skip);
    }

    public Object next() {
        Object object = ((IFn)const__2.getRawRoot()).invoke(this.iter_atom, this.next_skip);
        return object != null && object != Boolean.FALSE ? this : null;
    }

    public Object get() {
        db$filter_retractions$reify__12652 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.iter_atom));
    }
}

