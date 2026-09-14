/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Datom;

public final class integrity$unpaired_history_retractions$fn__22103
extends AFunction {
    Object progress;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"retract-assert-pair?");

    public integrity$unpaired_history_retractions$fn__22103(Object object) {
        this.progress = object;
    }

    public Object invoke(Object d1, Object d2) {
        Object object;
        ((IFn)this_.progress).invoke();
        boolean or__5238__auto__22105 = ((Datom)d1).added();
        if (or__5238__auto__22105) {
            object = or__5238__auto__22105 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object2 = d1;
            d1 = null;
            Object object3 = d2;
            d2 = null;
            integrity$unpaired_history_retractions$fn__22103 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object2, object3);
        }
        return object;
    }
}

