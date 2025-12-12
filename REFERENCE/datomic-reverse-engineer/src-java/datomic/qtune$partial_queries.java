/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.qtune$partial_queries$fn__23433;

public final class qtune$partial_queries
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object query2, Object preds, Object clauses, Object rclauses, Object allow_cross) {
        Object object = allow_cross;
        allow_cross = null;
        Object object2 = query2;
        query2 = null;
        Object object3 = clauses;
        clauses = null;
        Object object4 = preds;
        preds = null;
        qtune$partial_queries$fn__23433 qtune$partial_queries$fn__23433 = new qtune$partial_queries$fn__23433(object, rclauses, object2, object3, object4);
        Object object5 = rclauses;
        rclauses = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)qtune$partial_queries$fn__23433, (Object)PersistentArrayMap.EMPTY, object5);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return qtune$partial_queries.invokeStatic(object6, object7, object8, object9, object10);
    }
}

