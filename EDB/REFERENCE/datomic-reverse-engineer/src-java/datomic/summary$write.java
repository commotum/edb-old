/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.io.Writer;

public final class summary$write
extends AFunction {
    public static Object invokeStatic(Object w, Object o) {
        Object object = w;
        w = null;
        Object object2 = o;
        o = null;
        ((Writer)object).write((String)object2);
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return summary$write.invokeStatic(object3, object4);
    }
}

