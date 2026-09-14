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
import datomic.catalog$db_ids$fn__11084;

public final class catalog$db_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"datomic.catalog", (String)"db-names");

    public static Object invokeStatic(Object catalog2) {
        catalog$db_ids$fn__11084 catalog$db_ids$fn__11084 = new catalog$db_ids$fn__11084(catalog2);
        Object object = catalog2;
        catalog2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)catalog$db_ids$fn__11084, ((IFn)const__2.getRawRoot()).invoke(object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return catalog$db_ids.invokeStatic(object2);
    }
}

