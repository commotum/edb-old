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
 *  com.github.benmanes.caffeine.cache.CacheLoader
 *  com.github.benmanes.caffeine.cache.Caffeine
 */
package datomic.cache;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import datomic.cache.caffeine$create_computing$reify__622;

public final class caffeine$create_computing
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache.caffeine", (String)"adapt-caffeine-cache");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 153, RT.keyword(null, (String)"column"), 15});

    public static Object invokeStatic(Object f, Object max_size) {
        Object object = max_size;
        max_size = null;
        Object object2 = f;
        f = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)Caffeine.newBuilder().maximumSize(RT.longCast((Object)((Number)object))).build((CacheLoader)((IObj)new caffeine$create_computing$reify__622(null, object2)).withMeta((IPersistentMap)const__5)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return caffeine$create_computing.invokeStatic(object3, object4);
    }
}

