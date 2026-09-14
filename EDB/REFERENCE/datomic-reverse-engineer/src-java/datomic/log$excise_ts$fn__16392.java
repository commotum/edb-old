/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$excise_ts$fn__16392$fn__16393;

public final class log$excise_ts$fn__16392
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"datomic.excise", (String)"datoms");

    public Object invoke(Object s, Object p) {
        Object object = s;
        s = null;
        Object object2 = p;
        p = null;
        log$excise_ts$fn__16392 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke((Object)new log$excise_ts$fn__16392$fn__16393(), ((IFn)const__2.getRawRoot()).invoke(object2)));
    }
}

