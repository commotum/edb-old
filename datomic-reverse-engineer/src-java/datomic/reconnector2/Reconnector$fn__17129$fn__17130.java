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
package datomic.reconnector2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Reconnector$fn__17129$fn__17130
extends AFunction {
    Object state;
    Object cleanup_fn;
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__2 = RT.var((String)"datomic.monitor", (String)"alarm");
    public static final Keyword const__3 = RT.keyword(null, (String)"UnhandledException");

    public Reconnector$fn__17129$fn__17130(Object object, Object object2) {
        this.state = object;
        this.cleanup_fn = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)this.cleanup_fn).invoke(this.state);
        }
        catch (Throwable t__9147__auto__2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.reconnector2");
            Throwable ex = t__9147__auto__2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__0.getRawRoot()).invoke((Object)"error executing future"), ex);
                Logger logger2 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__1.getRawRoot()).invoke((Object)logger2, (Object)throwable);
            }
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3);
            Object t__9147__auto__2 = null;
            throw t__9147__auto__2;
        }
        return object;
    }
}

