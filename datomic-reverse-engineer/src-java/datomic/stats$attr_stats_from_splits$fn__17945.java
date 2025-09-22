/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.stats$attr_stats_from_splits$fn__17945$fn__17946;

public final class stats$attr_stats_from_splits$fn__17945
extends AFunction {
    Object db;
    public static final Keyword const__0 = RT.keyword(null, (String)"count");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.stats", (String)"db-attr-splits");

    public stats$attr_stats_from_splits$fn__17945(Object object) {
        this.db = object;
    }

    public Object invoke(Object attr) {
        Object object = attr;
        Object[] objectArray = new Object[2];
        objectArray[0] = const__0;
        Object object2 = attr;
        attr = null;
        objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke((Object)new stats$attr_stats_from_splits$fn__17945$fn__17946(), ((IFn)const__4.getRawRoot()).invoke(this.db, object2)));
        return Tuple.create((Object)object, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

