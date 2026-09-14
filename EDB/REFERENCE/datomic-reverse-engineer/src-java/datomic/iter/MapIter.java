/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.iter;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.iter.Iter;

public final class MapIter
implements Iter,
IType {
    public final Object f;
    Object iter;

    public MapIter(Object object, Object object2) {
        this.f = object;
        this.iter = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object next() {
        MapIter mapIter;
        Object temp__5457__auto__11740;
        Object object = temp__5457__auto__11740 = ((Iter)this.iter).next();
        if (object != null && object != Boolean.FALSE) {
            Object n;
            Object object2 = temp__5457__auto__11740;
            temp__5457__auto__11740 = null;
            Object object3 = n = object2;
            n = null;
            this.iter = object3;
            mapIter = this;
        } else {
            mapIter = null;
        }
        return mapIter;
    }

    public Object get() {
        MapIter this_ = null;
        return ((IFn)this_.f).invoke(((Iter)this_.iter).get());
    }
}

