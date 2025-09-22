/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.log$ensure_tombstone$fn__20596;

public final class log$ensure_tombstone
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object log2, Object tombstone) {
        Object c__10230__auto__20632 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object captured_bindings__10231__auto__20633 = Var.getThreadBindingFrame();
        Object object = tombstone;
        tombstone = null;
        Object object2 = log2;
        log2 = null;
        Object object3 = captured_bindings__10231__auto__20633;
        captured_bindings__10231__auto__20633 = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new log$ensure_tombstone$fn__20596(object, object2, c__10230__auto__20632, object3));
        Object var2_2 = null;
        return c__10230__auto__20632;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$ensure_tombstone.invokeStatic(object3, object4);
    }
}

