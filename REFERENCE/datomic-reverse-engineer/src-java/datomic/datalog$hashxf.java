/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog$hashxf$fn__18112;

public final class datalog$hashxf
extends AFunction {
    public static Object invokeStatic(Object bindings) {
        Object object = bindings;
        bindings = null;
        return new datalog$hashxf$fn__18112(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$hashxf.invokeStatic(object2);
    }
}

