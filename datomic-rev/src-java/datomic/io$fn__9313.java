/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.net.URL;

public final class io$fn__9313
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return ((URL)object).toURI();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$fn__9313.invokeStatic(object2);
    }
}

