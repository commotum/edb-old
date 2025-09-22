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
import datomic.core2.val_store.s3$go_with_metrics$fn__21378;

public final class s3$go_with_metrics
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object f, Object k, Object op, Object context) {
        Object c__10230__auto__21406 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object captured_bindings__10231__auto__21407 = Var.getThreadBindingFrame();
        Object object = op;
        op = null;
        Object object2 = context;
        context = null;
        Object object3 = captured_bindings__10231__auto__21407;
        captured_bindings__10231__auto__21407 = null;
        Object object4 = k;
        k = null;
        Object object5 = f;
        f = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new s3$go_with_metrics$fn__21378(object, object2, c__10230__auto__21406, object3, object4, object5));
        Object object6 = c__10230__auto__21406;
        c__10230__auto__21406 = null;
        return object6;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return s3$go_with_metrics.invokeStatic(object5, object6, object7, object8);
    }
}

