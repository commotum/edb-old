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

public final class db$t_needing_index_job
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"t-needing-avet");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"t-needing-excise");

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object or__5238__auto__13176;
        Object object2 = or__5238__auto__13176 = ((IFn)const__0.getRawRoot()).invoke(db2, d);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__13176;
            or__5238__auto__13176 = null;
        } else {
            Object object3 = db2;
            db2 = null;
            Object object4 = d;
            d = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$t_needing_index_job.invokeStatic(object3, object4);
    }
}

