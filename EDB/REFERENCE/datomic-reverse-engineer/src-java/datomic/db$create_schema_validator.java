/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db$create_schema_validator$fn__13273;

public final class db$create_schema_validator
extends AFunction {
    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return new db$create_schema_validator$fn__13273(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$create_schema_validator.invokeStatic(object2);
    }
}

