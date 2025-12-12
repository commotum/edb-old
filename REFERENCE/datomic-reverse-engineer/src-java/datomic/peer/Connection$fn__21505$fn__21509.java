/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.peer;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Connection$fn__21505$fn__21509
extends AFunction {
    Object recon;
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"peer", (String)"transactor-connection-failed")});
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deref");

    public Connection$fn__21505$fn__21509(Object object) {
        this.recon = object;
    }

    public Object invoke(Object ex) {
        Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
        Object object = ex;
        ex = null;
        Object ex2 = object;
        if (logger.isInfoEnabled()) {
            logger.info((String)((IFn)const__0.getRawRoot()).invoke((Object)const__3), ex2);
            Logger logger2 = logger;
            logger = null;
            Object object2 = ex2;
            ex2 = null;
            ((IFn)const__4.getRawRoot()).invoke((Object)logger2, object2);
        }
        Connection$fn__21505$fn__21509 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke(this_.recon);
    }
}

