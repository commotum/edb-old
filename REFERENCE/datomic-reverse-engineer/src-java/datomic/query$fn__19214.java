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
import datomic.query.EMapImpl;
import datomic.query.EntityMap;

public final class query$fn__19214
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"print-method");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"merge");

    public static Object invokeStatic(Object m, Object w) {
        Object object = ((EMapImpl)m).cache();
        Object object2 = m;
        m = null;
        Object object3 = w;
        w = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, ((EntityMap)object2).edits), object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$fn__19214.invokeStatic(object3, object4);
    }
}

