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
import datomic.aggregation$max$fn__17088;
import datomic.aggregation$max$fn__17090;

public final class aggregation$max
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"sort");

    public static Object invokeStatic(Object n, Object coll) {
        Object object = n;
        n = null;
        Object object2 = coll;
        coll = null;
        return ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object, ((IFn)const__4.getRawRoot()).invoke((Object)new aggregation$max$fn__17090(), object2)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aggregation$max.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object coll) {
        Object object = ((IFn)const__1.getRawRoot()).invoke(coll);
        Object object2 = coll;
        coll = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new aggregation$max$fn__17088(), object, object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$max.invokeStatic(object2);
    }
}

