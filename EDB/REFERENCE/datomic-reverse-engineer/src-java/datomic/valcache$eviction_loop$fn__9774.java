/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
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
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.valcache$eviction_loop$fn__9774$fn__9775;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$eviction_loop$fn__9774
extends AFunction {
    Object dirs;
    Object evict1;
    long counter_base;
    long start;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mod");
    public static final Object const__3 = 128L;
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__6 = RT.keyword(null, (String)"event");
    public static final Keyword const__7 = RT.keyword(null, (String)"counter");
    public static final Keyword const__8 = RT.keyword(null, (String)"iter");
    public static final Keyword const__9 = RT.keyword(null, (String)"msec");
    public static final Object const__13 = 4096L;
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge-with");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"+");

    public valcache$eviction_loop$fn__9774(Object object, Object object2, long l, long l2) {
        this.dirs = object;
        this.evict1 = object2;
        this.counter_base = l;
        this.start = l2;
    }

    public Object invoke() {
        long counter = this_.counter_base;
        long iter2 = 0L;
        Object summary2 = PersistentArrayMap.EMPTY;
        while (true) {
            Object temp__5455__auto__9778;
            Object object = temp__5455__auto__9778 = ((IFn)new valcache$eviction_loop$fn__9774$fn__9775(this_.dirs, this_.evict1, counter)).invoke();
            if (object == null || object == Boolean.FALSE) break;
            Object object2 = temp__5455__auto__9778;
            temp__5455__auto__9778 = null;
            Object step = object2;
            if (Numbers.isZero((Object)((IFn)const__2.getRawRoot()).invoke((Object)Numbers.num((long)counter), const__3))) {
                Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    logger2.info((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__6, "ValcacheEvictProgress", const__7, Numbers.num((long)counter), const__8, Numbers.num((long)iter2), const__9, Numbers.num((long)Numbers.minus((long)System.currentTimeMillis(), (long)this_.start))}), summary2)));
                }
            }
            PersistentArrayMap persistentArrayMap = summary2;
            summary2 = null;
            Object object3 = step;
            step = null;
            summary2 = ((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), (Object)persistentArrayMap, object3);
            iter2 = Numbers.inc((long)iter2);
            counter = RT.longCast((Object)((IFn)const__2.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.inc((long)counter)), const__13));
        }
        if (iter2 == 0L) {
        } else {
            Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
            if (logger.isInfoEnabled()) {
                Logger logger3 = logger;
                logger = null;
                Object object = summary2;
                summary2 = null;
                logger3.info((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__6, "ValcacheEvictCompleted", const__7, Numbers.num((long)counter), const__8, Numbers.num((long)iter2), const__9, Numbers.num((long)Numbers.minus((long)System.currentTimeMillis(), (long)this_.start))}), object)));
            }
        }
        valcache$eviction_loop$fn__9774 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.inc((long)counter)), const__13);
    }
}

