/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Util;

public final class h2$uq
extends AFunction {
    public static Object invokeStatic(Object username) {
        Object object;
        if (Util.equiv((Object)"", (Object)username)) {
            object = "\"\"";
        } else {
            object = username;
            Object object2 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$uq.invokeStatic(object2);
    }
}

