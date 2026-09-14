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

public final class integrity$unpaired_history_assertions$fn__22097
extends AFunction {
    Object progress;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"retract-assert-pair?");

    public integrity$unpaired_history_assertions$fn__22097(Object object) {
        this.progress = object;
    }

    public Object invoke(Object d1, Object d2) {
        Object object;
        Object or__5238__auto__22099;
        ((IFn)this_.progress).invoke();
        Object object2 = or__5238__auto__22099 = ((IFn)const__0.getRawRoot()).invoke((Object)(((Datom)d2).added() ? Boolean.TRUE : Boolean.FALSE));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__22099;
            or__5238__auto__22099 = null;
        } else {
            Object object3 = d1;
            d1 = null;
            Object object4 = d2;
            d2 = null;
            integrity$unpaired_history_assertions$fn__22097 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4);
        }
        return object;
    }
}

