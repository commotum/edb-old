/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class slf4j$trace
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"log-expr");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"trace");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"trace");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object msg, Object ex) {
        Object object = msg;
        msg = null;
        Object object2 = ex;
        ex = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__2, object, object2);
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
        return slf4j$trace.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object msg) {
        Object object = msg;
        msg = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, object);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return slf4j$trace.invokeStatic(object4, object5, object6);
    }
}

