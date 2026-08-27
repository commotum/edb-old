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
import datomic.stats$db_attr_stats$fn__17886;

public final class stats$db_attr_stats
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"merge-with");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__5 = RT.var((String)"datomic.stats", (String)"tiers");

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(const__1.getRawRoot(), const__3.getRawRoot()), ((IFn)const__4.getRawRoot()).invoke((Object)new stats$db_attr_stats$fn__17886(object), const__5.getRawRoot()));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return stats$db_attr_stats.invokeStatic(object2);
    }
}

