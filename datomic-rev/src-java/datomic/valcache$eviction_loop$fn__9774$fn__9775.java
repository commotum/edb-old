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

public final class valcache$eviction_loop$fn__9774$fn__9775
extends AFunction {
    Object dirs;
    Object evict1;
    long counter;
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public valcache$eviction_loop$fn__9774$fn__9775(Object object, Object object2, long l) {
        this.dirs = object;
        this.evict1 = object2;
        this.counter = l;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)this.evict1).invoke(RT.nth((Object)this.dirs, (int)RT.intCast((long)this.counter)));
        }
        catch (Throwable ex2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
            Object ex2 = null;
            Throwable ex3 = ex2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__1.getRawRoot()).invoke((Object)"ValcacheEvictLoopFailed"), ex3);
                Logger logger2 = logger;
                logger = null;
                Throwable throwable = ex3;
                ex3 = null;
                ((IFn)const__2.getRawRoot()).invoke((Object)logger2, (Object)throwable);
            }
            object = null;
        }
        return object;
    }
}

