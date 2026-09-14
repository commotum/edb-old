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

public final class common$qualified_symbol_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"namespace");

    public static Object invokeStatic(Object x) {
        Object object;
        Object and__5236__auto__9129;
        Object object2 = and__5236__auto__9129 = ((IFn)const__1.getRawRoot()).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object and__5236__auto__9128;
            Object object3 = x;
            x = null;
            Object object4 = and__5236__auto__9128 = ((IFn)const__2.getRawRoot()).invoke(object3);
            if (object4 != null && object4 != Boolean.FALSE) {
                object = Boolean.TRUE;
            } else {
                object = and__5236__auto__9128;
                Object var2_2 = null;
            }
        } else {
            object = and__5236__auto__9129;
            Object var1_1 = null;
        }
        return RT.booleanCast((Object)object) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$qualified_symbol_QMARK_.invokeStatic(object2);
    }
}

