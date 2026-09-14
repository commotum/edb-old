/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogImpl$fn__16309$fn__16310;

public final class LogImpl$fn__16309
extends AFunction {
    Object olookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");

    public LogImpl$fn__16309(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object k) {
        Object object = k;
        k = null;
        LogImpl$fn__16309 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new LogImpl$fn__16309$fn__16310(), RT.get((Object)this_.olookup, (Object)object));
    }
}

