/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.aws;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ddb$item_map$fn__20432
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__4 = RT.var((String)"datomic.core2.aws.ddb", (String)"attribute-value");

    public Object invoke(Object m, Object k, Object v) {
        if (Util.identical((Object)v, null)) {
            throw (Throwable)new RuntimeException((String)((IFn)const__1.getRawRoot()).invoke((Object)"No value for ", k));
        }
        Object object = m;
        m = null;
        Object object2 = k;
        k = null;
        Object object3 = v;
        v = null;
        ddb$item_map$fn__20432 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke(object, ((IFn)const__3.getRawRoot()).invoke(object2), ((IFn)const__4.getRawRoot()).invoke(object3));
    }
}

