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

public final class uri$fn__16984$fn__16987
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"name");

    public Object invoke(Object p__16986) {
        Object object = p__16986;
        p__16986 = null;
        Object vec__16988 = object;
        Object k = RT.nth((Object)vec__16988, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16988;
        vec__16988 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object object4 = v;
        v = null;
        uri$fn__16984$fn__16987 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3), (Object)"=", object4);
    }
}

