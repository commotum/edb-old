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

public final class uri$query_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"replace");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object), (Object)"-", (Object)"_");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$query_key.invokeStatic(object2);
    }
}

