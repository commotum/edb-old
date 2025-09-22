/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.io.Writer;

public final class reconnector2$fn__17144
extends AFunction {
    public static Object invokeStatic(Object o, Object w) {
        Object object = w;
        w = null;
        Object object2 = o;
        o = null;
        ((Writer)object).write(object2.toString());
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return reconnector2$fn__17144.invokeStatic(object3, object4);
    }
}

