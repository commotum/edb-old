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
import datomic.excise.ExcisePred;

public final class excise$pred_and_extent$reify__14840
implements ExcisePred,
IObj {
    final IPersistentMap __meta;
    Object remove_QMARK_;
    Object datoms;

    public excise$pred_and_extent$reify__14840(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.remove_QMARK_ = object;
        this.datoms = object2;
    }

    public excise$pred_and_extent$reify__14840(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new excise$pred_and_extent$reify__14840(iPersistentMap, this.remove_QMARK_, this.datoms);
    }

    public Object ep_remove_QMARK_(Object d) {
        Object object = d;
        d = null;
        excise$pred_and_extent$reify__14840 this_ = null;
        return ((IFn)this_.remove_QMARK_).invoke(object);
    }

    public Object ep_datoms() {
        excise$pred_and_extent$reify__14840 this_ = null;
        return ((IFn)this_.datoms).invoke();
    }
}

