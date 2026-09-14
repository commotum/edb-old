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
import datomic.log.LogDir;

public final class LogImpl$fn__16307
extends AFunction {
    Object olookup;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");

    public LogImpl$fn__16307(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object k) {
        Object object = k;
        k = null;
        return ((IFn)const__2.getRawRoot()).invoke(RT.get((Object)this.olookup, (Object)object)) instanceof LogDir ? Boolean.TRUE : Boolean.FALSE;
    }
}

