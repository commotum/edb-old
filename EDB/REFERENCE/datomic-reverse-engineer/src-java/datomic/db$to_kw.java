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

public final class db$to_kw
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"caching-normalize");

    public static Object invokeStatic(Object kw) {
        Object object;
        Object object2 = kw;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(kw);
            if (object3 != null && object3 != Boolean.FALSE) {
                object = kw;
                kw = null;
            } else {
                Object object4 = kw;
                kw = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object4);
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$to_kw.invokeStatic(object2);
    }
}

