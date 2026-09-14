/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$lift_consts_from_preds$fn__18662;

public final class datalog$lift_consts_from_preds
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"clojure.core", (String)"vector");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"keys");

    public static Object invokeStatic(Object clauses) {
        Object object;
        Object vec__18657 = ((IFn)const__0.getRawRoot()).invoke((Object)new datalog$lift_consts_from_preds$fn__18662(), (Object)Tuple.create((Object)PersistentArrayMap.EMPTY, (Object)PersistentVector.EMPTY), clauses);
        Object subs = RT.nth((Object)vec__18657, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__18657;
        vec__18657 = null;
        Object new_clauses = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = ((IFn)const__4.getRawRoot()).invoke(subs);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = clauses;
            clauses = null;
        } else {
            Object object4 = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)const__12), ((IFn)const__13.getRawRoot()).invoke(subs))));
            Object object5 = subs;
            subs = null;
            Object object6 = new_clauses;
            new_clauses = null;
            object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object4, ((IFn)const__11.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object5)))))))), object6));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$lift_consts_from_preds.invokeStatic(object2);
    }
}

