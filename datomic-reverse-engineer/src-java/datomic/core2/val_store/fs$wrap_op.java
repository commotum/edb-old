/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.val_store.fs$wrap_op$fn__21262;

public final class fs$wrap_op
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store.fs", (String)"wrap-metric-handler");

    public static Object invokeStatic(Object f, Object k, Object op) {
        Object object = f;
        f = null;
        Object object2 = k;
        k = null;
        Object object3 = op;
        op = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new fs$wrap_op$fn__21262(object), object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fs$wrap_op.invokeStatic(object4, object5, object6);
    }
}

