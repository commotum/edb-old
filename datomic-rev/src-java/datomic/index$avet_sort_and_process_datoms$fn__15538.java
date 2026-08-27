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

public final class index$avet_sort_and_process_datoms$fn__15538
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"event");
    public static final Keyword const__3 = RT.keyword((String)"index", (String)"external-sort");

    public Object invoke(Object m) {
        block0: {
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            if (!logger.isInfoEnabled()) break block0;
            Logger logger2 = logger;
            logger = null;
            Object object = m;
            m = null;
            logger2.info((String)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)const__2, (Object)const__3)));
        }
        return null;
    }
}

