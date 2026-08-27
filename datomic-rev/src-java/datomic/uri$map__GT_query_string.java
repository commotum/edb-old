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
import datomic.uri$map__GT_query_string$fn__17035;

public final class uri$map__GT_query_string
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"second");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)"&", ((IFn)const__1.getRawRoot()).invoke((Object)new uri$map__GT_query_string$fn__17035(), ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$map__GT_query_string.invokeStatic(object2);
    }
}

