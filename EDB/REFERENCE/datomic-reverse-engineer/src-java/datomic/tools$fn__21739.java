/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class tools$fn__21739
extends AFunction {
    public static Object invokeStatic(Object report2, Object data2) {
        Object object = report2;
        report2 = null;
        Object object2 = data2;
        data2 = null;
        return ((IFn)object).invoke(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$fn__21739.invokeStatic(object3, object4);
    }
}

