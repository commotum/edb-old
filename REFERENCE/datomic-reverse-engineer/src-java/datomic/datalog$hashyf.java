/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog$hashyf$fn__18115;

public final class datalog$hashyf
extends AFunction {
    public static Object invokeStatic(Object bindings) {
        Object object = bindings;
        bindings = null;
        return new datalog$hashyf$fn__18115(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$hashyf.invokeStatic(object2);
    }
}

