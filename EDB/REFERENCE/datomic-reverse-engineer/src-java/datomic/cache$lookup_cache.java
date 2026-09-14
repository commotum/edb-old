/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cache$lookup_cache$reify__9407;

public final class cache$lookup_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"lookup-cache");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 131, RT.keyword(null, (String)"column"), 6});

    public static Object invokeStatic(Object m, Object cache2, Object f) {
        Object object = m;
        m = null;
        Object object2 = f;
        f = null;
        Object object3 = cache2;
        cache2 = null;
        return ((IObj)new cache$lookup_cache$reify__9407(null, object, object2, object3)).withMeta((IPersistentMap)const__5);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cache$lookup_cache.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object m, Object cache2) {
        Object object = m;
        m = null;
        Object object2 = cache2;
        cache2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cache$lookup_cache.invokeStatic(object3, object4);
    }
}

