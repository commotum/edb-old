/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.atom.logged$append_value$fn__19744;

public final class logged$append_value
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object log2, Object serialize, Object header, Object value) {
        Object c__10230__auto__19776 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object captured_bindings__10231__auto__19777 = Var.getThreadBindingFrame();
        Object object = value;
        value = null;
        Object object2 = log2;
        log2 = null;
        Object object3 = serialize;
        serialize = null;
        Object object4 = captured_bindings__10231__auto__19777;
        captured_bindings__10231__auto__19777 = null;
        Object object5 = header;
        header = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new logged$append_value$fn__19744(c__10230__auto__19776, object, object2, object3, object4, object5));
        Object object6 = c__10230__auto__19776;
        c__10230__auto__19776 = null;
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
        return logged$append_value.invokeStatic(object5, object6, object7, object8);
    }
}

