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

public final class valcache$fn__9659
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"noop-quit");

    public static Object invokeStatic(Object header, Object sc) {
        Object object = header;
        header = null;
        Object object2 = sc;
        sc = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$fn__9659.invokeStatic(object3, object4);
    }
}

