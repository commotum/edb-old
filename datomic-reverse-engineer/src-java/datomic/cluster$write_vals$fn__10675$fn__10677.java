/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class cluster$write_vals$fn__10675$fn__10677
extends AFunction {
    public static final Var const__3 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");

    public Object invoke(Object p__10676) {
        Object object = p__10676;
        p__10676 = null;
        Object vec__10678 = object;
        Object k = RT.nth((Object)vec__10678, (int)RT.intCast((long)0L), null);
        Object object2 = vec__10678;
        vec__10678 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object object4 = v;
        v = null;
        return Tuple.create((Object)((IFn)const__3.getRawRoot()).invoke(object3), (Object)object4);
    }
}

