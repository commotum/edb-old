/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class kv_sql_ext$fn__11548
extends AFunction {
    public static Object invokeStatic(Object _) {
        return "select 1 from dual";
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql_ext$fn__11548.invokeStatic(object2);
    }
}

