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
import datomic.core2.async$put_all_BANG_$fn__19616;

public final class async$put_all_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.async", (String)"put-all!");
    public static final Var const__1 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__2 = 1L;
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object ch, Object coll, Object close_QMARK_) {
        Object c__10230__auto__19651 = ((IFn)const__1.getRawRoot()).invoke(const__2);
        Object captured_bindings__10231__auto__19652 = Var.getThreadBindingFrame();
        Object object = close_QMARK_;
        close_QMARK_ = null;
        Object object2 = captured_bindings__10231__auto__19652;
        captured_bindings__10231__auto__19652 = null;
        Object object3 = coll;
        coll = null;
        Object object4 = ch;
        ch = null;
        ((IFn)const__3.getRawRoot()).invoke((Object)new async$put_all_BANG_$fn__19616(object, object2, object3, c__10230__auto__19651, object4));
        Object var3_3 = null;
        return c__10230__auto__19651;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return async$put_all_BANG_.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object ch, Object coll) {
        Object object = ch;
        ch = null;
        Object object2 = coll;
        coll = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)Boolean.TRUE);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return async$put_all_BANG_.invokeStatic(object3, object4);
    }
}

