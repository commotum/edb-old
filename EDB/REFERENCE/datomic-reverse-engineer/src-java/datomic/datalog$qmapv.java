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

public final class datalog$qmapv
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"on-query-thread?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"pooled-mapv");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"query-pool");

    public static Object invokeStatic(Object f, Object coll) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke();
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = f;
            f = null;
            Object object4 = coll;
            coll = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4);
        } else {
            Object object5 = f;
            f = null;
            Object object6 = coll;
            coll = null;
            object = ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), object5, object6);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$qmapv.invokeStatic(object3, object4);
    }
}

