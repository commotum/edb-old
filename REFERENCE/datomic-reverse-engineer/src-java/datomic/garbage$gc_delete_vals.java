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
import datomic.garbage$gc_delete_vals$fn__19836;
import datomic.garbage$gc_delete_vals$fn__19838;

public final class garbage$gc_delete_vals
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Object const__4 = 0L;
    public static final Var const__5 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__6 = RT.keyword(null, (String)"GarbageDeletedCount");

    public static Object invokeStatic(Object cluster2, Object vs) {
        Object futs;
        Object object = cluster2;
        cluster2 = null;
        Object object2 = vs;
        vs = null;
        Object object3 = futs = ((IFn)const__0.getRawRoot()).invoke((Object)new garbage$gc_delete_vals$fn__19836(object), ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object2));
        futs = null;
        Object result2 = ((IFn)const__3.getRawRoot()).invoke((Object)new garbage$gc_delete_vals$fn__19838(), const__4, object3);
        ((IFn)const__5.getRawRoot()).invoke((Object)const__6, result2);
        Object var3_3 = null;
        return result2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$gc_delete_vals.invokeStatic(object3, object4);
    }
}

