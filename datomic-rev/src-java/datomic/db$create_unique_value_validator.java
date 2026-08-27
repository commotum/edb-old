/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db$create_unique_value_validator$fn__13309;
import java.util.HashMap;

public final class db$create_unique_value_validator
extends AFunction {
    public static Object invokeStatic(Object db2) {
        HashMap avmap = new HashMap();
        Object object = db2;
        db2 = null;
        HashMap hashMap = avmap;
        avmap = null;
        return new db$create_unique_value_validator$fn__13309(object, hashMap);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$create_unique_value_validator.invokeStatic(object2);
    }
}

