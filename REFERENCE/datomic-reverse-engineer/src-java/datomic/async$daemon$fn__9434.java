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

public final class async$daemon$fn__9434
extends AFunction {
    Object bound_f;
    Object name;
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"daemon", (String)"thread-started");
    public static final Keyword const__3 = RT.keyword(null, (String)"name");
    public static final Keyword const__4 = RT.keyword((String)"daemon", (String)"thread-completed");

    public async$daemon$fn__9434(Object object, Object object2) {
        this.bound_f = object;
        this.name = object2;
    }

    public Object invoke() {
        Object object;
        Logger logger = LoggerFactory.getLogger((String)"datomic.async");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, this.name})));
        }
        try {
            object = ((IFn)this.bound_f).invoke();
        }
        catch (Throwable throwable) {
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.async");
            if (logger3.isInfoEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                logger4.info((String)((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__4, const__3, this.name})));
            }
            throw throwable;
        }
        Logger logger5 = LoggerFactory.getLogger((String)"datomic.async");
        if (logger5.isInfoEnabled()) {
            Logger logger6 = logger5;
            logger5 = null;
            logger6.info((String)((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__4, const__3, this.name})));
        }
        return object;
    }
}

