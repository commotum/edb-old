/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.iter;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.iter.Iter;

public final class ReversedIter
implements Iter,
IType {
    Object iter;

    public ReversedIter(Object object) {
        this.iter = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object next() {
        ReversedIter reversedIter;
        Object temp__5457__auto__11786;
        Object object = temp__5457__auto__11786 = ((Iter)this.iter).prev();
        if (object != null && object != Boolean.FALSE) {
            Object it;
            Object object2 = temp__5457__auto__11786;
            temp__5457__auto__11786 = null;
            Object object3 = it = object2;
            it = null;
            this.iter = object3;
            reversedIter = this;
        } else {
            reversedIter = null;
        }
        return reversedIter;
    }

    public Object get() {
        return ((Iter)this.iter).get();
    }
}

