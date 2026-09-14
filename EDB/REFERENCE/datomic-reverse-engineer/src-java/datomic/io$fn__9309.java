/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.net.URI;

public final class io$fn__9309
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return new URI((String)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$fn__9309.invokeStatic(object2);
    }
}

