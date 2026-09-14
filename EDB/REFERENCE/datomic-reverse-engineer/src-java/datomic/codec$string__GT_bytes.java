/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class codec$string__GT_bytes
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return ((String)object).getBytes("UTF-8");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return codec$string__GT_bytes.invokeStatic(object2);
    }
}

