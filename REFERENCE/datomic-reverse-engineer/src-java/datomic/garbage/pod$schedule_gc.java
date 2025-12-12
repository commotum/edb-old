/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.garbage;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.garbage.pod$schedule_gc$fn__22693;

public final class pod$schedule_gc
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object cluster2, Object garbage_ids_ref) {
        Object c__6597__auto__22740 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object captured_bindings__6598__auto__22741 = Var.getThreadBindingFrame();
        Object object = garbage_ids_ref;
        garbage_ids_ref = null;
        Object object2 = captured_bindings__6598__auto__22741;
        captured_bindings__6598__auto__22741 = null;
        Object object3 = cluster2;
        cluster2 = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new pod$schedule_gc$fn__22693(c__6597__auto__22740, object, object2, object3));
        Object var2_2 = null;
        return c__6597__auto__22740;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pod$schedule_gc.invokeStatic(object3, object4);
    }
}

