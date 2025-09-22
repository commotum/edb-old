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

public final class db$user_proc_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"var?");

    public static Object invokeStatic(Object procid) {
        Object object = procid;
        procid = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$user_proc_QMARK_.invokeStatic(object2);
    }
}

