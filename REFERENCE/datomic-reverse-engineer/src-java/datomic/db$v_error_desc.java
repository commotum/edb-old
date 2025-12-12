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

public final class db$v_error_desc
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__6 = 0L;
    public static final Object const__7 = 61L;

    public static Object invokeStatic(Object v) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(v);
        if (object2 != null && object2 != Boolean.FALSE) {
            if (64L < (long)RT.count((Object)v)) {
                Object object3 = v;
                v = null;
                object = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object3, const__6, const__7), (Object)"...");
            } else {
                object = v;
                v = null;
            }
        } else {
            object = v;
            Object object4 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$v_error_desc.invokeStatic(object2);
    }
}

