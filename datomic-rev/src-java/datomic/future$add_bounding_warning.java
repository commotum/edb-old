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
import datomic.future$add_bounding_warning$fn__10223;

public final class future$add_bounding_warning
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object promise_ch, Object context, Object seconds) {
        Object c__6597__auto__10259 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object captured_bindings__6598__auto__10260 = Var.getThreadBindingFrame();
        Object object = seconds;
        seconds = null;
        Object object2 = promise_ch;
        promise_ch = null;
        Object object3 = context;
        context = null;
        Object object4 = captured_bindings__6598__auto__10260;
        captured_bindings__6598__auto__10260 = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new future$add_bounding_warning$fn__10223(c__6597__auto__10259, object, object2, object3, object4));
        Object var3_3 = null;
        return c__6597__auto__10259;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return future$add_bounding_warning.invokeStatic(object4, object5, object6);
    }
}

