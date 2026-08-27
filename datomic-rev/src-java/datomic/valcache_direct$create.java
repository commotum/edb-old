/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.valcache_direct.ValcacheDirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache_direct$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"path");
    public static final Keyword const__4 = RT.keyword(null, (String)"puts-pool");
    public static final Var const__5 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__6 = RT.keyword(null, (String)"event");
    public static final Keyword const__7 = RT.keyword((String)"valcache-direct", (String)"start");
    public static final Keyword const__8 = RT.keyword(null, (String)"root");
    public static final Var const__9 = RT.var((String)"datomic.valcache", (String)"direct-init");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__11 = RT.var((String)"datomic.valcache.puts-pool-impl", (String)"valcache-puts-pool");

    public static Object invokeStatic(Object p__9927) {
        Object object;
        Object or__5238__auto__9930;
        Object map__9928;
        Object object2;
        Object object3 = p__9927;
        p__9927 = null;
        Object map__99282 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__99282);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__99282;
            map__99282 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__99282;
            map__99282 = null;
        }
        Object args = map__9928 = object2;
        Object path2 = RT.get((Object)map__9928, (Object)const__3);
        Object object6 = map__9928;
        map__9928 = null;
        Object puts_pool2 = RT.get((Object)object6, (Object)const__4);
        Logger logger = LoggerFactory.getLogger((String)"datomic.valcache-direct");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__5.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__6, const__7, const__8, path2})));
        }
        Object object7 = args;
        args = null;
        Object shutdown_fn = ((IFn)const__9.getRawRoot()).invoke(object7);
        Object object8 = path2;
        path2 = null;
        Object object9 = shutdown_fn;
        shutdown_fn = null;
        Object object10 = puts_pool2;
        puts_pool2 = null;
        Object object11 = or__5238__auto__9930 = object10;
        if (object11 != null && object11 != Boolean.FALSE) {
            object = or__5238__auto__9930;
            or__5238__auto__9930 = null;
        } else {
            object = ((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot());
        }
        return new ValcacheDirect(object8, object9, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache_direct$create.invokeStatic(object2);
    }
}

