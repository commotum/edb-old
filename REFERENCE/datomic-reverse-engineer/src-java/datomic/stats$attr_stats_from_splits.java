/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$attr_stats_from_splits$fn__17945;

public final class stats$attr_stats_from_splits
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__3 = RT.var((String)"datomic.stats", (String)"db-attr-stats");

    public static Object invokeStatic(Object db2) {
        stats$attr_stats_from_splits$fn__17945 stats$attr_stats_from_splits$fn__17945 = new stats$attr_stats_from_splits$fn__17945(db2);
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)stats$attr_stats_from_splits$fn__17945), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return stats$attr_stats_from_splits.invokeStatic(object2);
    }
}

