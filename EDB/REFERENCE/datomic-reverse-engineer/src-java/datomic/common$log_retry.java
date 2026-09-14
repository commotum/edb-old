/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class common$log_retry
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"common", (String)"retry");
    public static final Keyword const__2 = RT.keyword(null, (String)"backoff");
    public static final Keyword const__3 = RT.keyword(null, (String)"attempts");
    public static final Keyword const__4 = RT.keyword(null, (String)"max-retries");
    public static final Var const__7 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__10 = RT.keyword(null, (String)"result");

    public static Object invokeStatic(Object result2, Object backoff, Object attempts, Object max_retries) {
        Object v8;
        Object[] objectArray = new Object[8];
        objectArray[0] = const__0;
        objectArray[1] = const__1;
        objectArray[2] = const__2;
        Object object = backoff;
        backoff = null;
        objectArray[3] = object;
        objectArray[4] = const__3;
        Object object2 = attempts;
        attempts = null;
        objectArray[5] = object2;
        objectArray[6] = const__4;
        Object object3 = max_retries;
        max_retries = null;
        objectArray[7] = object3;
        IPersistentMap m = RT.mapUniqueKeys((Object[])objectArray);
        if (result2 instanceof Throwable) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.common");
            Object object4 = result2;
            result2 = null;
            Object ex = object4;
            if (logger.isInfoEnabled()) {
                IPersistentMap iPersistentMap = m;
                m = null;
                logger.info((String)((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap), ex);
                Logger logger2 = logger;
                logger = null;
                Object object5 = ex;
                ex = null;
                ((IFn)const__8.getRawRoot()).invoke((Object)logger2, object5);
            }
            v8 = null;
        } else {
            Logger logger = LoggerFactory.getLogger((String)"datomic.common");
            if (logger.isInfoEnabled()) {
                Logger logger3 = logger;
                logger = null;
                IPersistentMap iPersistentMap = m;
                m = null;
                Object object6 = result2;
                result2 = null;
                logger3.info((String)((IFn)const__7.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)iPersistentMap, (Object)const__10, object6)));
            }
            v8 = null;
        }
        return v8;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return common$log_retry.invokeStatic(object5, object6, object7, object8);
    }
}

