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

public final class ddb_s3_cluster$create_s3_store$retry_fn__22791
extends AFunction {
    Object retry_opts;
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store.s3", (String)"retry-handler");

    public ddb_s3_cluster$create_s3_store$retry_fn__22791(Object object) {
        this.retry_opts = object;
    }

    public Object invoke(Object f, Object op) {
        Object object = f;
        f = null;
        Object object2 = op;
        op = null;
        ddb_s3_cluster$create_s3_store$retry_fn__22791 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, this_.retry_opts);
    }
}

