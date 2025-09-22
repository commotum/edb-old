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

public final class SharedCriticalFailure$fn__14953
extends AFunction {
    Object msg;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*err*");

    public SharedCriticalFailure$fn__14953(Object object) {
        this.msg = object;
    }

    public Object invoke() {
        this.msg = null;
        Object s = ((IFn)const__0.getRawRoot()).invoke((Object)"Terminating process - ", this.msg);
        Logger logger = LoggerFactory.getLogger((String)"datomic.process");
        if (logger.isErrorEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.error((String)((IFn)const__1.getRawRoot()).invoke(s));
        }
        Object object = s;
        s = null;
        ((PrintWriter)const__2.get()).println((String)object);
        return null;
    }
}

