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

public final class slf4j$print_and_warn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");

    public static Object invokeStatic(Object msg) {
        block0: {
            ((IFn)const__0.getRawRoot()).invoke(msg);
            Logger logger = LoggerFactory.getLogger((String)"datomic.slf4j");
            if (!logger.isWarnEnabled()) break block0;
            Logger logger2 = logger;
            logger = null;
            Object object = msg;
            msg = null;
            logger2.warn((String)((IFn)const__1.getRawRoot()).invoke(object));
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return slf4j$print_and_warn.invokeStatic(object2);
    }
}

