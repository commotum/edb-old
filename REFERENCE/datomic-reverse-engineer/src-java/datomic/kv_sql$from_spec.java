/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_sql.KVSql;

public final class kv_sql$from_spec
extends AFunction {
    public static Object invokeStatic(Object spec) {
        Object object = spec;
        spec = null;
        return new KVSql(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql$from_spec.invokeStatic(object2);
    }
}

