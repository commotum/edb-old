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

public final class valcache$dirname
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"format");

    public static Object invokeStatic(Object n) {
        Object object = n;
        n = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)"%03x", object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$dirname.invokeStatic(object2);
    }
}

