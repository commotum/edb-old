/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db$create_op_validator$fn__13301;
import java.util.HashMap;

public final class db$create_op_validator
extends AFunction {
    public static Object invokeStatic(Object db2) {
        HashMap eavmap;
        HashMap hashMap = eavmap = new HashMap();
        eavmap = null;
        Object object = db2;
        db2 = null;
        return new db$create_op_validator$fn__13301(hashMap, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$create_op_validator.invokeStatic(object2);
    }
}

