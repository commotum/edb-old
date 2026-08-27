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

public final class cleanup$fn__20749$fn__20751$fn__20752
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public Object invoke(Object t) {
        Object object = t;
        t = null;
        cleanup$fn__20749$fn__20751$fn__20752 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }
}

