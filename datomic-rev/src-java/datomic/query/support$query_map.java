/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class support$query_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"sequential?");
    public static final Var const__3 = RT.var((String)"datomic.query.support", (String)"listq->mapq");

    public static Object invokeStatic(Object q2) {
        Object object;
        Object object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(q2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = q2;
            q2 = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke(object4);
        } else {
            object2 = q2;
            q2 = null;
        }
        Object q3 = object2;
        Object object5 = ((IFn)const__2.getRawRoot()).invoke(q3);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = q3;
            q3 = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object6);
        } else {
            object = q3;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return support$query_map.invokeStatic(object2);
    }
}

