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

public final class query$parse_query
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"mapify-query");
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"process-self-unifications");
    public static final Var const__2 = RT.var((String)"datomic.query", (String)"process-find-bindings");
    public static final Var const__3 = RT.var((String)"datomic.query", (String)"process-pulls");
    public static final Var const__4 = RT.var((String)"datomic.query", (String)"process-aggregates");
    public static final Var const__5 = RT.var((String)"datomic.query", (String)"process-in-bindings");
    public static final Var const__6 = RT.var((String)"datomic.query", (String)"process-ranges");
    public static final Var const__7 = RT.var((String)"datomic.query", (String)"validate-query");
    public static final Var const__8 = RT.var((String)"datomic.query", (String)"resolve-qualified-fns");

    public static Object invokeStatic(Object query2) {
        Object query3;
        Object query4;
        Object query5;
        Object query6;
        Object query7;
        Object query8;
        Object query9;
        Object object = query2;
        query2 = null;
        Object object2 = query9 = ((IFn)const__0.getRawRoot()).invoke(object);
        query9 = null;
        Object object3 = query8 = ((IFn)const__1.getRawRoot()).invoke(object2);
        query8 = null;
        Object object4 = query7 = ((IFn)const__2.getRawRoot()).invoke(object3);
        query7 = null;
        Object object5 = query6 = ((IFn)const__3.getRawRoot()).invoke(object4);
        query6 = null;
        Object object6 = query5 = ((IFn)const__4.getRawRoot()).invoke(object5);
        query5 = null;
        Object object7 = query4 = ((IFn)const__5.getRawRoot()).invoke(object6, (Object)"$__in__");
        query4 = null;
        Object object8 = query3 = ((IFn)const__6.getRawRoot()).invoke(object7);
        query3 = null;
        Object query10 = ((IFn)const__7.getRawRoot()).invoke(object8);
        ((IFn)const__8.getRawRoot()).invoke(query10);
        Object object9 = query10;
        query10 = null;
        return object9;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$parse_query.invokeStatic(object2);
    }
}

