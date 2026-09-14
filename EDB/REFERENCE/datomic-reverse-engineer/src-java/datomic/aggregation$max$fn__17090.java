/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aggregation$max$fn__17090
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"compare");

    public Object invoke(Object p1__17087_SHARP_, Object p2__17086_SHARP_) {
        Object object = p2__17086_SHARP_;
        p2__17086_SHARP_ = null;
        Object object2 = p1__17087_SHARP_;
        p1__17087_SHARP_ = null;
        return Numbers.num((long)((IFn.OOL)const__0.getRawRoot()).invokePrim(object, object2));
    }
}

