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

public final class sql$update$fn__11497
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"name");

    public Object invoke(Object p__11496) {
        Object object = p__11496;
        p__11496 = null;
        Object vec__11498 = object;
        Object k = RT.nth((Object)vec__11498, (int)RT.intCast((long)0L), null);
        Object object2 = vec__11498;
        vec__11498 = null;
        RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        sql$update$fn__11497 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3), (Object)"=?");
    }
}
