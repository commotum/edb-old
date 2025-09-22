/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class support$listq__GT_mapq$fn__19068
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object m, Object p__19067) {
        Object object = p__19067;
        p__19067 = null;
        Object vec__19069 = object;
        Object k = RT.nth((Object)vec__19069, (int)RT.intCast((long)0L), null);
        Object object2 = vec__19069;
        vec__19069 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object k2 = ((IFn)const__3.getRawRoot()).invoke(object3);
        Object object4 = m;
        m = null;
        Object object5 = k2;
        k2 = null;
        Object object6 = v;
        v = null;
        support$listq__GT_mapq$fn__19068 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(object4, object5, object6);
    }
}

