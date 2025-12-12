/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cache$repairing_cache_stack$reify__9424;

public final class cache$repairing_cache_stack
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"cache-1");
    public static final Keyword const__4 = RT.keyword(null, (String)"cache-2");
    public static final Keyword const__5 = RT.keyword(null, (String)"close-cache-1?");
    public static final Keyword const__6 = RT.keyword(null, (String)"close-cache-2?");
    public static final Keyword const__7 = RT.keyword(null, (String)"on-repair");
    public static final AFn const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 260, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object p__9422) {
        Object on_repair;
        Object object;
        Object object2 = p__9422;
        p__9422 = null;
        Object map__9423 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__9423);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__9423;
            map__9423 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__9423;
            map__9423 = null;
        }
        Object map__94232 = object;
        Object cache_1 = RT.get((Object)map__94232, (Object)const__3);
        Object cache_2 = RT.get((Object)map__94232, (Object)const__4);
        Object close_cache_1_QMARK_ = RT.get((Object)map__94232, (Object)const__5, (Object)Boolean.TRUE);
        Object close_cache_2_QMARK_ = RT.get((Object)map__94232, (Object)const__6, (Object)Boolean.TRUE);
        Object object5 = map__94232;
        map__94232 = null;
        Object object6 = on_repair = RT.get((Object)object5, (Object)const__7);
        on_repair = null;
        Object object7 = cache_1;
        cache_1 = null;
        Object object8 = cache_2;
        cache_2 = null;
        Object object9 = close_cache_1_QMARK_;
        close_cache_1_QMARK_ = null;
        Object object10 = close_cache_2_QMARK_;
        close_cache_2_QMARK_ = null;
        return ((IObj)new cache$repairing_cache_stack$reify__9424(null, object6, object7, object8, object9, object10)).withMeta((IPersistentMap)const__12);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cache$repairing_cache_stack.invokeStatic(object2);
    }
}

