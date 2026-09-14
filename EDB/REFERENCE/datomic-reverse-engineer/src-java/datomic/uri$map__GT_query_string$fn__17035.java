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

public final class uri$map__GT_query_string$fn__17035
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"name");

    public Object invoke(Object p__17034) {
        Object object = p__17034;
        p__17034 = null;
        Object vec__17036 = object;
        Object k = RT.nth((Object)vec__17036, (int)RT.intCast((long)0L), null);
        Object object2 = vec__17036;
        vec__17036 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object object4 = v;
        v = null;
        uri$map__GT_query_string$fn__17035 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3), (Object)"=", object4);
    }
}

