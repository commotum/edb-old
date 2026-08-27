/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class kv_cluster$notify_retry
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__2 = RT.keyword(null, (String)"event");
    public static final Keyword const__3 = RT.keyword((String)"kv-cluster", (String)"retry");
    public static final Keyword const__4 = RT.keyword(null, (String)"attempts");
    public static final Keyword const__5 = RT.keyword(null, (String)"max-retries");
    public static final Keyword const__6 = RT.keyword(null, (String)"cause");
    public static final Var const__7 = RT.var((String)"datomic.kv-cluster", (String)"retry-cause");

    public static Object invokeStatic(Object result2, Object metric, Object backoff, Object attempts, Object max_retries) {
        block0: {
            ((IFn)const__0.getRawRoot()).invoke(metric, backoff);
            Logger logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
            if (!logger.isInfoEnabled()) break block0;
            Logger logger2 = logger;
            logger = null;
            Object[] objectArray = new Object[10];
            objectArray[0] = const__2;
            objectArray[1] = const__3;
            Object object = metric;
            metric = null;
            objectArray[2] = object;
            Object object2 = backoff;
            backoff = null;
            objectArray[3] = object2;
            objectArray[4] = const__4;
            Object object3 = attempts;
            attempts = null;
            objectArray[5] = object3;
            objectArray[6] = const__5;
            Object object4 = max_retries;
            max_retries = null;
            objectArray[7] = object4;
            objectArray[8] = const__6;
            Object object5 = result2;
            result2 = null;
            objectArray[9] = ((IFn)const__7.getRawRoot()).invoke(object5);
            logger2.info((String)((IFn)const__1.getRawRoot()).invoke((Object)RT.map((Object[])objectArray)));
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
        return kv_cluster$notify_retry.invokeStatic(object6, object7, object8, object9, object10);
    }
}

