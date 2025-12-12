/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cli$coerce_vals$fn__20718;

public final class cli$coerce_vals
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cli", (String)"unique-index");
    public static final Keyword const__1 = RT.keyword(null, (String)"long-name");
    public static final Keyword const__2 = RT.keyword(null, (String)"coerce");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object m, Object spec) {
        Object idx;
        Object object = spec;
        spec = null;
        Object object2 = idx = ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, (Object)const__2);
        idx = null;
        Object object3 = m;
        m = null;
        return ((IFn)const__3.getRawRoot()).invoke((Object)new cli$coerce_vals$fn__20718(object2), (Object)PersistentArrayMap.EMPTY, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cli$coerce_vals.invokeStatic(object3, object4);
    }
}

