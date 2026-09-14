/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 *  com.github.benmanes.caffeine.cache.Caffeine
 *  com.github.benmanes.caffeine.cache.Weigher
 */
package datomic.cache;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import datomic.cache.caffeine$create_weight_limited$reify__616;
import java.util.Arrays;

public final class caffeine$create_weight_limited
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"<");
    public static final Object const__1 = 0L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<"), 0L, Symbol.intern(null, (String)"weight"), Symbol.intern((String)"Integer", (String)"MAX_VALUE")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__5 = RT.var((String)"datomic.cache.caffeine", (String)"adapt-caffeine-cache");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 133, RT.keyword(null, (String)"column"), 17});

    public static Object invokeStatic(Object weight, Object f) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(const__1, weight, (Object)Integer.MAX_VALUE);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        }
        Object object2 = weight;
        weight = null;
        Object object3 = f;
        f = null;
        return ((IFn)const__5.getRawRoot()).invoke((Object)Caffeine.newBuilder().maximumWeight(RT.longCast((Object)((Number)object2))).weigher((Weigher)((IObj)new caffeine$create_weight_limited$reify__616(null, object3)).withMeta((IPersistentMap)const__10)).build());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return caffeine$create_weight_limited.invokeStatic(object3, object4);
    }
}

