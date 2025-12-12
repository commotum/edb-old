/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class common$closing_watch
extends AFunction {
    public static Object invokeStatic(Object _, Object _2, Object old, Object _3) {
        Object v2;
        Object object = old;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = old;
            old = null;
            ((AutoCloseable)object2).close();
            v2 = null;
        } else {
            v2 = null;
        }
        return v2;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return common$closing_watch.invokeStatic(object5, object6, object7, object8);
    }
}

