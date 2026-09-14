/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.integrity$mk_attr_pred$fn__22131;

public final class integrity$mk_attr_pred
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return new integrity$mk_attr_pred$fn__22131(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$mk_attr_pred.invokeStatic(object2);
    }
}

