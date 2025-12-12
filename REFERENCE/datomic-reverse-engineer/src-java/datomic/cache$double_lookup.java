/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.cache$double_lookup$reify__9419;

public final class cache$double_lookup
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 235, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object m1, Object m2) {
        Object object = m2;
        m2 = null;
        Object object2 = m1;
        m1 = null;
        return ((IObj)new cache$double_lookup$reify__9419(null, object, object2)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cache$double_lookup.invokeStatic(object3, object4);
    }
}

