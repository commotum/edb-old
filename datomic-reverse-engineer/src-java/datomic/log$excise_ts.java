/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$excise_ts$fn__16392;

public final class log$excise_ts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object log2, Object xpreds) {
        Object object = xpreds;
        xpreds = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new log$excise_ts$fn__16392(), (Object)PersistentHashSet.EMPTY, object);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$excise_ts.invokeStatic(object3, object4);
    }
}

