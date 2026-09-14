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

public final class query$pattern_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"source?");

    public static Object invokeStatic(Object s) {
        Object object;
        Object and__5236__auto__19376;
        Object object2 = and__5236__auto__19376 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(s));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = s;
            s = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object3));
        } else {
            object = and__5236__auto__19376;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$pattern_QMARK_.invokeStatic(object2);
    }
}

