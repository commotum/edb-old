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

public final class connector$fn__21177$fn__21178
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.transaction", (String)"peer-message-type");

    public Object invoke(Object m, Object conn) {
        Object object = m;
        m = null;
        connector$fn__21177$fn__21178 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }
}

