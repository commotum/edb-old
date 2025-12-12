/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aggregation$min$fn__17083
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"compare");

    public Object invoke(Object x, Object y) {
        Object object;
        if (((IFn.OOL)const__1.getRawRoot()).invokePrim(x, y) < 0L) {
            object = x;
            x = null;
        } else {
            object = y;
            Object var2_2 = null;
        }
        return object;
    }
}

