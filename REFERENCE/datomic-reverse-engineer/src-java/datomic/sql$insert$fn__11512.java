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

public final class sql$insert$fn__11512
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");

    public Object invoke(Object p__11511) {
        Object object = p__11511;
        p__11511 = null;
        Object vec__11513 = object;
        Object k = RT.nth((Object)vec__11513, (int)RT.intCast((long)0L), null);
        Object object2 = vec__11513;
        vec__11513 = null;
        RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        sql$insert$fn__11512 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object3);
    }
}

