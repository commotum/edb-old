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
package datomic.garbage;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class pod$warn
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public static Object invokeStatic(Object msg, Object ex) {
        block0: {
            Logger logger = LoggerFactory.getLogger((String)"datomic.garbage.pod");
            Object object = ex;
            ex = null;
            Object ex2 = object;
            if (!logger.isWarnEnabled()) break block0;
            Object object2 = msg;
            msg = null;
            logger.warn((String)((IFn)const__0.getRawRoot()).invoke(object2), ex2);
            Logger logger2 = logger;
            logger = null;
            Object object3 = ex2;
            ex2 = null;
            ((IFn)const__1.getRawRoot()).invoke((Object)logger2, object3);
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pod$warn.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object msg) {
        block0: {
            Logger logger = LoggerFactory.getLogger((String)"datomic.garbage.pod");
            if (!logger.isWarnEnabled()) break block0;
            Logger logger2 = logger;
            logger = null;
            Object object = msg;
            msg = null;
            logger2.warn((String)((IFn)const__0.getRawRoot()).invoke(object));
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pod$warn.invokeStatic(object2);
    }
}

