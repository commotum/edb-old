/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class qtune$aug
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"println");
    public static final Object const__3 = 1L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__6 = RT.var((String)"datomic.qtune", (String)"partial-queries");
    public static final Var const__8 = RT.var((String)"datomic.qtune", (String)"min-ret");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"disj");

    public static Object invokeStatic(Object query2, Object preds, Object clauses, Object rclauses, Object args) {
        long i = 0L;
        Object object = clauses;
        clauses = null;
        Object clauses2 = object;
        Object object2 = rclauses;
        rclauses = null;
        Object rclauses2 = object2;
        while (true) {
            Object qs;
            ((IFn)const__1.getRawRoot()).invoke((Object)"Slot: ", (Object)Numbers.num((long)i));
            if (1L == (long)RT.count((Object)rclauses2)) break;
            Object object3 = qs = ((IFn)const__6.getRawRoot()).invoke(query2, preds, clauses2, rclauses2, (Object)(Numbers.isZero((long)i) ? Boolean.TRUE : Boolean.FALSE));
            qs = null;
            Object clause = ((IFn)const__8.getRawRoot()).invoke(object3, args, const__3);
            Object object4 = clauses2;
            clauses2 = null;
            Object object5 = ((IFn)const__10.getRawRoot()).invoke(object4, clause);
            Object object6 = rclauses2;
            rclauses2 = null;
            Object object7 = clause;
            clause = null;
            rclauses2 = ((IFn)const__11.getRawRoot()).invoke(object6, object7);
            clauses2 = object5;
            i = Numbers.inc((long)i);
        }
        Object object8 = clauses2;
        clauses2 = null;
        Object object9 = rclauses2;
        rclauses2 = null;
        return ((IFn)const__5.getRawRoot()).invoke(object8, object9);
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
        return qtune$aug.invokeStatic(object6, object7, object8, object9, object10);
    }
}

