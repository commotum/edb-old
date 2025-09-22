/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.algo.lazy$fully_partition_by$fpb__19419;

public final class lazy$fully_partition_by
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");

    /*
     * WARNING - void declaration
     */
    public static Object invokeStatic(Object f, Object coll) {
        void fpb;
        lazy$fully_partition_by$fpb__19419 lazy$fully_partition_by$fpb__19419 = null;
        lazy$fully_partition_by$fpb__19419 = new lazy$fully_partition_by$fpb__19419();
        Object object = f;
        f = null;
        Object object2 = coll;
        coll = null;
        return ((IFn)fpb).invoke(object, ((IFn)const__0.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return lazy$fully_partition_by.invokeStatic(object3, object4);
    }
}

