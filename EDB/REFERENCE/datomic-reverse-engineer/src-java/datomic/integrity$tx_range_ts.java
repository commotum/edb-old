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

public final class integrity$tx_range_ts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__1 = RT.keyword(null, (String)"t");
    public static final Var const__2 = RT.var((String)"datomic.api", (String)"tx-range");
    public static final Var const__3 = RT.var((String)"datomic.api", (String)"log");
    public static final Object const__4 = 1000L;
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"next-t");
    public static final Var const__6 = RT.var((String)"datomic.api", (String)"db");

    public static Object invokeStatic(Object conn) {
        Object object = ((IFn)const__3.getRawRoot()).invoke(conn);
        Object object2 = conn;
        conn = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke(object, const__4, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object2))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$tx_range_ts.invokeStatic(object2);
    }
}

