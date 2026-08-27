/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class stats$db_stats
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"db-attr-stats");
    public static final Keyword const__1 = RT.keyword(null, (String)"datoms");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"transduce");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__4 = RT.keyword(null, (String)"count");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"vals");
    public static final Keyword const__7 = RT.keyword(null, (String)"attrs");

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        Object attr_stats = ((IFn)const__0.getRawRoot()).invoke(object);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        objectArray[1] = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)const__4), const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke(attr_stats));
        objectArray[2] = const__7;
        Object object2 = attr_stats;
        attr_stats = null;
        objectArray[3] = object2;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return stats$db_stats.invokeStatic(object2);
    }
}

