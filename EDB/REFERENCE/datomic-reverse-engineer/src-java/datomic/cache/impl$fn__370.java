/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.RT;

public final class impl$fn__370
extends AFunction {
    public static Object invokeStatic(Object coll) {
        Object object = coll;
        coll = null;
        return RT.count((Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return impl$fn__370.invokeStatic(object2);
    }
}

