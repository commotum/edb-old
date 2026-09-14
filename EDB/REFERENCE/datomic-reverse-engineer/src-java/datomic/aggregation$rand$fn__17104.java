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

public final class aggregation$rand$fn__17104
extends AFunction {
    Object coll;
    public static final Var const__0 = RT.var((String)"datomic.aggregation", (String)"rand");

    public aggregation$rand$fn__17104(Object object) {
        this.coll = object;
    }

    public Object invoke() {
        aggregation$rand$fn__17104 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.coll);
    }
}

