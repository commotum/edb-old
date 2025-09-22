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

public final class peer$create_connection$fn__21595
extends AFunction {
    Object lucene_queue;
    Object db_ref;
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"integrate-lucene");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public peer$create_connection$fn__21595(Object object, Object object2) {
        this.lucene_queue = object;
        this.db_ref = object2;
    }

    public Object invoke() {
        Object var2_4;
        try {
            ((IFn)const__0.getRawRoot()).invoke(this.db_ref, this.lucene_queue);
            Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__1.getRawRoot()).invoke((Object)"Shutting down lucene integration thread"));
            }
            var2_4 = null;
        }
        catch (Throwable t2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
            Object t2 = null;
            Throwable ex = t2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__1.getRawRoot()).invoke((Object)"Peer fulltext integration failed"), ex);
                Logger logger3 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__2.getRawRoot()).invoke((Object)logger3, (Object)throwable);
            }
            var2_4 = null;
        }
        return var2_4;
    }
}

