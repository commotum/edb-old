/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$prep_clauses$fn__18723;

public final class datalog$prep_clauses
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"normalize-or-join");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"not-or->not-or-join");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"not-or-all-pred");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__9 = RT.var((String)"datomic.datalog", (String)"expr-clause");
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"lift-consts-from-preds");

    public static Object invokeStatic(Object rm, Object clauses) {
        Object object = clauses;
        clauses = null;
        Object clauses2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(const__3.getRawRoot(), object)));
        Object object2 = rm;
        rm = null;
        Object object3 = clauses2;
        clauses2 = null;
        Object vec__18719 = ((IFn)const__4.getRawRoot()).invoke((Object)new datalog$prep_clauses$fn__18723(), (Object)Tuple.create((Object)object2, (Object)PersistentVector.EMPTY), object3);
        Object rm2 = RT.nth((Object)vec__18719, (int)RT.uncheckedIntCast((long)0L), null);
        Object object4 = vec__18719;
        vec__18719 = null;
        Object clauses3 = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)1L), null);
        Object object5 = rm2;
        rm2 = null;
        Object object6 = clauses3;
        clauses3 = null;
        return Tuple.create((Object)object5, (Object)((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__10.getRawRoot()).invoke(object6)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$prep_clauses.invokeStatic(object3, object4);
    }
}

