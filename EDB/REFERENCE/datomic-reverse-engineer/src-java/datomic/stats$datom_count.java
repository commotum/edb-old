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
import datomic.stats$datom_count$fn__17980;

public final class stats$datom_count
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__5 = RT.keyword(null, (String)"data-count");

    public static Object invokeStatic(Object sizes2) {
        Object object = sizes2;
        sizes2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke((Object)const__5, ((IFn)const__2.getRawRoot()).invoke((Object)new stats$datom_count$fn__17980(), object))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return stats$datom_count.invokeStatic(object2);
    }
}

