/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class slf4j$log_uncaught_exceptions$reify__8991
implements Thread.UncaughtExceptionHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public slf4j$log_uncaught_exceptions$reify__8991(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public slf4j$log_uncaught_exceptions$reify__8991() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new slf4j$log_uncaught_exceptions$reify__8991(iPersistentMap);
    }

    public void uncaughtException(Thread thread2, Throwable throwable) {
        block0: {
            Logger logger = LoggerFactory.getLogger((String)"datomic.slf4j");
            Throwable throwable2 = throwable;
            throwable = null;
            Throwable ex = throwable2;
            if (!logger.isWarnEnabled()) break block0;
            logger.warn((String)((IFn)const__0.getRawRoot()).invoke((Object)"Uncaught exception"), ex);
            Logger logger2 = logger;
            logger = null;
            Throwable throwable3 = ex;
            ex = null;
            ((IFn)const__1.getRawRoot()).invoke((Object)logger2, (Object)throwable3);
        }
    }
}

