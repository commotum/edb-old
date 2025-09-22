/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.catalog$conflict_check_fn$fn__11106;

public final class catalog$conflict_check_fn
extends AFunction {
    public static Object invokeStatic(Object db_name, Object db_id) {
        Object object = db_id;
        db_id = null;
        Object object2 = db_name;
        db_name = null;
        return new catalog$conflict_check_fn$fn__11106(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$conflict_check_fn.invokeStatic(object3, object4);
    }
}

