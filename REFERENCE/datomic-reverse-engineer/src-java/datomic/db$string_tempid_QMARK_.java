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

public final class db$string_tempid_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");

    public static Object invokeStatic(Object s) {
        Object object;
        Object and__5236__auto__12610;
        Object object2 = and__5236__auto__12610 = ((IFn)const__0.getRawRoot()).invoke(s);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = s;
            s = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)(((String)object3).startsWith(":") ? Boolean.TRUE : Boolean.FALSE));
        } else {
            object = and__5236__auto__12610;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$string_tempid_QMARK_.invokeStatic(object2);
    }
}

