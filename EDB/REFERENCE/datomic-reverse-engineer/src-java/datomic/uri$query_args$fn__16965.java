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

public final class uri$query_args$fn__16965
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"datomic.uri", (String)"query-key");

    public Object invoke(Object p__16964) {
        Object object = p__16964;
        p__16964 = null;
        Object vec__16966 = object;
        Object k = RT.nth((Object)vec__16966, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16966;
        vec__16966 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object object4 = v;
        v = null;
        uri$query_args$fn__16965 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3), (Object)"=", object4);
    }
}

