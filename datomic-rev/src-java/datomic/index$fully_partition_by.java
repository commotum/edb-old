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
import datomic.index$fully_partition_by$fpb__15365;

public final class index$fully_partition_by
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");

    /*
     * WARNING - void declaration
     */
    public static Object invokeStatic(Object f, Object coll) {
        void fpb;
        index$fully_partition_by$fpb__15365 index$fully_partition_by$fpb__15365 = null;
        index$fully_partition_by$fpb__15365 = new index$fully_partition_by$fpb__15365();
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
        return index$fully_partition_by.invokeStatic(object3, object4);
    }
}

