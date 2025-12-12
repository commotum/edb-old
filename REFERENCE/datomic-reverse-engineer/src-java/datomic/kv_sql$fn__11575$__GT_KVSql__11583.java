/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_sql.KVSql;

public final class kv_sql$fn__11575$__GT_KVSql__11583
extends AFunction {
    public Object invoke(Object spec) {
        Object object = spec;
        spec = null;
        return new KVSql(object);
    }
}

