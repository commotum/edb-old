/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class slf4j$fn__8989
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public static Object invokeStatic(Object e) {
        block0: {
            Logger logger = LoggerFactory.getLogger((String)"datomic.slf4j");
            Object object = e;
            e = null;
            Object ex = object;
            if (!logger.isWarnEnabled()) break block0;
            logger.warn((String)((IFn)const__0.getRawRoot()).invoke((Object)"Caught exception"), ex);
            Logger logger2 = logger;
            logger = null;
            Object object2 = ex;
            ex = null;
            ((IFn)const__1.getRawRoot()).invoke((Object)logger2, object2);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return slf4j$fn__8989.invokeStatic(object2);
    }
}

