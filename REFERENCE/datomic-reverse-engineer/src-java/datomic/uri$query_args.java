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
import datomic.uri$query_args$fn__16965;

public final class uri$query_args
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)"&", ((IFn)const__1.getRawRoot()).invoke((Object)new uri$query_args$fn__16965(), object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$query_args.invokeStatic(object2);
    }
}

