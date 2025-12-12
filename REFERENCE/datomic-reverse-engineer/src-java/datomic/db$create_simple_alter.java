/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db$create_simple_alter$fn__13202;

public final class db$create_simple_alter
extends AFunction {
    public static Object invokeStatic(Object elem_key) {
        Object object = elem_key;
        elem_key = null;
        return new db$create_simple_alter$fn__13202(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$create_simple_alter.invokeStatic(object2);
    }
}

