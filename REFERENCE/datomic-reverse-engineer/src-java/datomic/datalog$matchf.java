/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog$matchf$fn__18109;

public final class datalog$matchf
extends AFunction {
    public static Object invokeStatic(Object bindings) {
        Object object = bindings;
        bindings = null;
        return new datalog$matchf$fn__18109(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$matchf.invokeStatic(object2);
    }
}

