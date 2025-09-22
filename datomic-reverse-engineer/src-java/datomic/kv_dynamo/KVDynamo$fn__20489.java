/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.kv_dynamo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class KVDynamo$fn__20489
extends AFunction {
    Object client;
    Object val_map;
    Object table;
    public static final Var const__0 = RT.var((String)"datomic.ddb-values", (String)"put-value");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public KVDynamo$fn__20489(Object object, Object object2, Object object3) {
        this.client = object;
        this.val_map = object2;
        this.table = object3;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.client, this.table, this.val_map);
        }
        finally {
            ((IFn)const__1.getRawRoot()).invoke();
        }
        return object;
    }
}

