/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.query$mapify_query$fn__19396;
import datomic.query.Immutify;

public final class query$mapify_query
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object query) {
        v0 = ((IFn)query$mapify_query.const__0.getRawRoot()).invoke(query);
        if (v0 != null && v0 != Boolean.FALSE) {
            ((IFn)query$mapify_query.const__1.getRawRoot()).invoke(((IFn)query$mapify_query.const__2.getRawRoot()).invoke((Object)query$mapify_query.const__3, (Object)Boolean.FALSE));
            v1 = query;
            query = null;
            v2 = ((IFn)new query$mapify_query$fn__19396(v1)).invoke();
        } else {
            v2 = query;
            query = null;
        }
        v3 = query = v2;
        query = null;
        v4 = v3;
        if (Util.classOf((Object)v3) == query$mapify_query.__cached_class__0) ** GOTO lbl18
        if (!(v4 instanceof Immutify)) {
            v4 = v4;
            query$mapify_query.__cached_class__0 = Util.classOf((Object)v4);
lbl18:
            // 2 sources

            v5 = query$mapify_query.const__4.getRawRoot().invoke(v4);
        } else {
            v5 = ((Immutify)v4).immutify();
        }
        query = v5;
        v6 = ((IFn)query$mapify_query.const__5.getRawRoot()).invoke(query);
        if (v6 != null && v6 != Boolean.FALSE) {
            v7 = query;
            query = null;
            v8 = ((IFn)query$mapify_query.const__6.getRawRoot()).invoke(v7);
        } else {
            v8 = query;
            query = null;
        }
        query = v8;
        v9 = ((IFn)query$mapify_query.const__7.getRawRoot()).invoke(query);
        if (v9 == null || v9 == Boolean.FALSE) {
            throw (Throwable)new IllegalArgumentException("query must be a readable edn string, list, or map");
        }
        var3_3 = null;
        return query;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$mapify_query.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"string?");
        const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
        const__2 = RT.var((String)"clojure.core", (String)"hash-map");
        const__3 = RT.var((String)"clojure.core", (String)"*read-eval*");
        const__4 = RT.var((String)"datomic.query", (String)"immutify");
        const__5 = RT.var((String)"clojure.core", (String)"sequential?");
        const__6 = RT.var((String)"datomic.query", (String)"listq->mapq");
        const__7 = RT.var((String)"clojure.core", (String)"map?");
    }
}

