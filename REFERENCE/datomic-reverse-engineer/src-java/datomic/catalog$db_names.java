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

public final class catalog$db_names
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"keys");

    public static Object invokeStatic(Object catalog2) {
        Object object = catalog2;
        catalog2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return catalog$db_names.invokeStatic(object2);
    }
}

