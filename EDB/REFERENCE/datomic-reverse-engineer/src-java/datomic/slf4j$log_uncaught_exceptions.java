/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.slf4j$log_uncaught_exceptions$reify__8991;

public final class slf4j$log_uncaught_exceptions
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 211, RT.keyword(null, (String)"column"), 4});

    public static Object invokeStatic() {
        Thread.setDefaultUncaughtExceptionHandler((Thread.UncaughtExceptionHandler)((IObj)new slf4j$log_uncaught_exceptions$reify__8991(null)).withMeta((IPersistentMap)const__4));
        return null;
    }

    public Object invoke() {
        return slf4j$log_uncaught_exceptions.invokeStatic();
    }
}

