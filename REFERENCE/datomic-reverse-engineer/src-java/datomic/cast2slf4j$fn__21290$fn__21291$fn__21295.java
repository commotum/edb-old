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

public final class cast2slf4j$fn__21290$fn__21291$fn__21295
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");

    public Object invoke(Object event) {
        block0: {
            Logger logger = LoggerFactory.getLogger((String)"datomic.cast2slf4j");
            if (!logger.isInfoEnabled()) break block0;
            Logger logger2 = logger;
            logger = null;
            Object object = event;
            event = null;
            logger2.info((String)((IFn)const__0.getRawRoot()).invoke(object));
        }
        return null;
    }
}

