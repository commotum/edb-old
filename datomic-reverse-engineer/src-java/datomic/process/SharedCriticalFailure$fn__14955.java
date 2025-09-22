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
package datomic.process;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SharedCriticalFailure$fn__14955
extends AFunction {
    Object t;
    Object msg;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*err*");

    public SharedCriticalFailure$fn__14955(Object object, Object object2) {
        this.t = object;
        this.msg = object2;
    }

    public Object invoke() {
        this.msg = null;
        Object s = ((IFn)const__0.getRawRoot()).invoke((Object)"Terminating process - ", this.msg);
        Logger logger = LoggerFactory.getLogger((String)"datomic.process");
        Object ex = this.t;
        if (logger.isErrorEnabled()) {
            logger.error((String)((IFn)const__1.getRawRoot()).invoke(s), ex);
            Logger logger2 = logger;
            logger = null;
            Object object = ex;
            ex = null;
            ((IFn)const__2.getRawRoot()).invoke((Object)logger2, object);
        }
        Object object = s;
        s = null;
        ((PrintWriter)const__3.get()).println((String)object);
        this.t = null;
        ((Throwable)this.t).printStackTrace();
        return null;
    }
}

