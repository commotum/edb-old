/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.Callable;

public final class kv_sql_ext$cluster_conf__GT_spec$fn__11569
extends AFunction {
    Object factory;

    public kv_sql_ext$cluster_conf__GT_spec$fn__11569(Object object) {
        this.factory = object;
    }

    public Object invoke(Object _) {
        return ((Callable)this.factory).call();
    }
}

