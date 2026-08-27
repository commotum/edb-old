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
import datomic.Datom;

public final class tools$nohistory_checker$fn__21876
extends AFunction {
    Object attrs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");

    public tools$nohistory_checker$fn__21876(Object object) {
        this.attrs = object;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        tools$nohistory_checker$fn__21876 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.attrs, ((Datom)object).a());
    }
}

