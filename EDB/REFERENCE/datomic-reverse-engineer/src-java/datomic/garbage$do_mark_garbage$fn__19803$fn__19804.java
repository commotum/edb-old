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

public final class garbage$do_mark_garbage$fn__19803$fn__19804
extends AFunction {
    Object cluster;
    Object max_dir_size;
    Object lookup;
    Object leaf;
    public static final Var const__0 = RT.var((String)"datomic.garbage", (String)"append-leaf");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public garbage$do_mark_garbage$fn__19803$fn__19804(Object object, Object object2, Object object3, Object object4) {
        this.cluster = object;
        this.max_dir_size = object2;
        this.lookup = object3;
        this.leaf = object4;
    }

    public Object invoke() {
        Object object;
        try {
            this.leaf = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.cluster, this.lookup, this.leaf, this.max_dir_size);
        }
        catch (Throwable t2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.garbage");
            Object t2 = null;
            Throwable ex = t2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__1.getRawRoot()).invoke((Object)"Garbage write failed"), ex);
                Logger logger2 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__2.getRawRoot()).invoke((Object)logger2, (Object)throwable);
            }
            object = null;
        }
        return object;
    }
}

