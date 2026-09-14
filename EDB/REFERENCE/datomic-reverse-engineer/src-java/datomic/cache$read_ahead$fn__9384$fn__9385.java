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

public final class cache$read_ahead$fn__9384$fn__9385
extends AFunction {
    Object lookup;
    Object k;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"getx");

    public cache$read_ahead$fn__9384$fn__9385(Object object, Object object2) {
        this.lookup = object;
        this.k = object2;
    }

    public Object invoke() {
        cache$read_ahead$fn__9384$fn__9385 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.lookup, this_.k);
    }
}

