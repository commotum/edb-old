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

public final class db$data_needs_index_QMARK_$fn__13177
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"t-needing-index-job");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduced");

    public db$data_needs_index_QMARK_$fn__13177(Object object) {
        this.db = object;
    }

    public Object invoke(Object x, Object d) {
        Object object;
        Object object2 = d;
        d = null;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(this_.db, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            db$data_needs_index_QMARK_$fn__13177 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)Boolean.TRUE);
        } else {
            object = x;
            Object var1_1 = null;
        }
        return object;
    }
}

