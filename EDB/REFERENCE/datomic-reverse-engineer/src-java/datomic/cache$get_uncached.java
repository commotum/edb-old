/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.cache.ICachedLookup;

public final class cache$get_uncached
extends AFunction {
    public static Object invokeStatic(Object m, Object k, Object nf) {
        Object object;
        if (m instanceof ICachedLookup) {
            Object object2 = m;
            m = null;
            Object object3 = k;
            k = null;
            Object object4 = nf;
            nf = null;
            object = ((ICachedLookup)object2).valAtUncached(object3, object4);
        } else {
            Object object5 = m;
            m = null;
            Object object6 = k;
            k = null;
            Object object7 = nf;
            nf = null;
            object = RT.get((Object)object5, (Object)object6, (Object)object7);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cache$get_uncached.invokeStatic(object4, object5, object6);
    }
}

