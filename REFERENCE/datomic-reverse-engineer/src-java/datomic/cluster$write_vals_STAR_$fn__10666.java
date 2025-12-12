/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class cluster$write_vals_STAR_$fn__10666
extends AFunction {
    Object metric;
    Object event;
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"bounded-deref");
    public static final Var const__5 = RT.var((String)"datomic.cluster", (String)"BOUNDING_TIMEOUT_MSEC");
    public static final Keyword const__6 = RT.keyword(null, (String)"created");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__10 = RT.var((String)"datomic.cluster", (String)"segment-writes");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"inc");
    public static final Var const__12 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Var const__13 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__14 = RT.keyword(null, (String)"event");
    public static final Keyword const__15 = RT.keyword(null, (String)"msec");

    public cluster$write_vals_STAR_$fn__10666(Object object, Object object2) {
        this.metric = object;
        this.event = object2;
    }

    public Object invoke(Object p__10665) {
        Object ret;
        Object object = p__10665;
        p__10665 = null;
        Object vec__10667 = object;
        Object start = RT.nth((Object)vec__10667, (int)RT.intCast((long)0L), null);
        Object object2 = vec__10667;
        vec__10667 = null;
        Object object3 = ret = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        ret = null;
        if (Util.equiv((Object)((IFn)const__4.getRawRoot()).invoke(object3, const__5.getRawRoot()), (Object)const__6)) {
            Object object4 = start;
            start = null;
            Number nsec = Numbers.minus((long)System.nanoTime(), (Object)object4);
            Object msec = ((IFn)const__8.getRawRoot()).invoke((Object)nsec);
            ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), const__11.getRawRoot());
            Number number = nsec;
            nsec = null;
            ((IFn)const__12.getRawRoot()).invoke(this.metric, (Object)number);
            Logger logger = LoggerFactory.getLogger((String)"datomic.cluster");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__14;
                objectArray[1] = this.event;
                objectArray[2] = const__15;
                Object object5 = msec;
                msec = null;
                objectArray[3] = object5;
                logger2.debug((String)((IFn)const__13.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
        } else {
            throw (Throwable)new RuntimeException("Cluster write failed");
        }
        return null;
    }
}

