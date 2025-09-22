/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.valcache$eviction_loop$evict1__9765;
import datomic.valcache$eviction_loop$fn__9774;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$eviction_loop
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__1 = RT.classForName((String)"java.nio.file.LinkOption");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"shuffle");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__5 = 4096L;
    public static final Var const__6 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__7 = RT.keyword(null, (String)"event");
    public static final Keyword const__8 = RT.keyword(null, (String)"path");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__10 = RT.keyword(null, (String)"threshold-mb");
    public static final Keyword const__13 = RT.keyword(null, (String)"interval-secs");
    public static final Keyword const__14 = RT.keyword(null, (String)"file-window");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object path2, Object threshold, Object interval_secs, Object file_window, Object shutdown_requested) {
        Object opts = ((IFn)const__0.getRawRoot()).invoke(const__1, (Object)PersistentVector.EMPTY);
        Object dirs = ((IFn)const__2.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5)));
        Object object = opts;
        opts = null;
        valcache$eviction_loop$evict1__9765 evict12 = new valcache$eviction_loop$evict1__9765(object, file_window, threshold, path2);
        Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            Object[] objectArray = new Object[10];
            objectArray[0] = const__7;
            objectArray[1] = "ValcacheEvictPlan";
            objectArray[2] = const__8;
            Object object2 = path2;
            path2 = null;
            objectArray[3] = ((IFn)const__9.getRawRoot()).invoke(object2);
            objectArray[4] = const__10;
            Object object3 = threshold;
            threshold = null;
            objectArray[5] = Numbers.quotient((Object)object3, (long)0x100000L);
            objectArray[6] = const__13;
            objectArray[7] = interval_secs;
            objectArray[8] = const__14;
            Object object4 = file_window;
            file_window = null;
            objectArray[9] = object4;
            logger2.info((String)((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
        }
        long counter_base = 0L;
        while (true) {
            Object object5 = ((IFn)const__16.getRawRoot()).invoke(shutdown_requested);
            if (object5 != null && object5 != Boolean.FALSE) break;
            long start = System.currentTimeMillis();
            Object next_base = ((IFn)new valcache$eviction_loop$fn__9774(dirs, (Object)evict12, counter_base, start)).invoke();
            Thread.sleep(RT.longCast((Object)Numbers.multiply((long)1000L, (Object)interval_secs)));
            Object object6 = next_base;
            next_base = null;
            counter_base = RT.longCast((Object)object6);
        }
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return valcache$eviction_loop.invokeStatic(object6, object7, object8, object9, object10);
    }
}

