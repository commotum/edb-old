/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$db_attr_splits$fn__17930;
import datomic.stats$db_attr_splits$fn__17941;

public final class stats$db_attr_splits
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"partition-all");
    public static final Object const__3 = 2L;
    public static final Object const__4 = 1L;
    public static final Var const__5 = RT.var((String)"datomic.stats", (String)"merge-splits");
    public static final Var const__6 = RT.var((String)"datomic.stats", (String)"tiers");

    public static Object invokeStatic(Object db2, Object attr) {
        Object object = attr;
        attr = null;
        Object object2 = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new stats$db_attr_splits$fn__17930()), ((IFn)const__2.getRawRoot()).invoke(const__3, const__4, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new stats$db_attr_splits$fn__17941(object, object2), const__6.getRawRoot()))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return stats$db_attr_splits.invokeStatic(object3, object4);
    }
}

