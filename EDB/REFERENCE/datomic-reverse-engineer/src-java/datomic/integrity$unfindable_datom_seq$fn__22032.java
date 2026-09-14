/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Util;
import datomic.iter.Iter;

public final class integrity$unfindable_datom_seq$fn__22032
extends AFunction {
    Object op;
    Object progress;
    Object db;

    public integrity$unfindable_datom_seq$fn__22032(Object object, Object object2, Object object3) {
        this.op = object;
        this.progress = object2;
        this.db = object3;
    }

    public Object invoke(Object datom) {
        Object object = this_.progress;
        if (object != null && object != Boolean.FALSE) {
            ((IFn)this_.progress).invoke();
        }
        Object object2 = datom;
        Object object3 = datom;
        datom = null;
        integrity$unfindable_datom_seq$fn__22032 this_ = null;
        return Util.equiv((Object)object2, (Object)((Iter)((IFn)this_.op).invoke(this_.db, object3)).get()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

