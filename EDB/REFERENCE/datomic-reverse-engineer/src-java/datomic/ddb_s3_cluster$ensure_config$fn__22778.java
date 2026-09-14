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

public final class ddb_s3_cluster$ensure_config$fn__22778
extends AFunction {
    Object new_conf;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public ddb_s3_cluster$ensure_config$fn__22778(Object object) {
        this.new_conf = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.new_conf);
        }
        finally {
            ((IFn)const__1.getRawRoot()).invoke();
        }
        return object;
    }
}

