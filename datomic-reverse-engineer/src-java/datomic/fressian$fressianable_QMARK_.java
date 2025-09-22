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
import datomic.fressian$fressianable_QMARK_$fn__12221;

public final class fressian$fressianable_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"fressianable?");
    public static final Var const__1 = RT.var((String)"datomic.fressian", (String)"clojure-write-handlers");

    public static Object invokeStatic(Object val, Object handlers) {
        Object object = handlers;
        handlers = null;
        Object object2 = val;
        val = null;
        return RT.booleanCast((Object)((IFn)new fressian$fressianable_QMARK_$fn__12221(object, object2)).invoke()) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fressian$fressianable_QMARK_.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object val) {
        Object object = val;
        val = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$fressianable_QMARK_.invokeStatic(object2);
    }
}

