/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datalog$recursive_QMARK_$fn__18607;

public final class datalog$recursive_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"recursive?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"extensional?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"rest");

    public static Object invokeStatic(Object prog, Object pred2, Object seen) {
        Object object;
        Object object2;
        Object or__5238__auto__18610;
        Object object3 = or__5238__auto__18610 = ((IFn)const__1.getRawRoot()).invoke(pred2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__18610;
            or__5238__auto__18610 = null;
        } else {
            object2 = ((IFn)const__2.getRawRoot()).invoke(prog, pred2);
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = Boolean.FALSE;
        } else {
            Object or__5238__auto__18611;
            Object object4 = or__5238__auto__18611 = ((IFn)const__3.getRawRoot()).invoke(seen, pred2);
            if (object4 != null && object4 != Boolean.FALSE) {
                object = or__5238__auto__18611;
                or__5238__auto__18611 = null;
            } else {
                Object object5 = seen;
                seen = null;
                Object seen2 = ((IFn)const__4.getRawRoot()).invoke(object5, pred2);
                Object object6 = pred2;
                pred2 = null;
                Object rules = RT.get((Object)prog, (Object)object6);
                Object object7 = prog;
                prog = null;
                Object object8 = seen2;
                seen2 = null;
                Object object9 = rules;
                rules = null;
                object = ((IFn)const__6.getRawRoot()).invoke((Object)new datalog$recursive_QMARK_$fn__18607(object7, object8), ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__9.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), object9))));
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datalog$recursive_QMARK_.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object prog, Object pred2) {
        Object object = prog;
        prog = null;
        Object object2 = pred2;
        pred2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)PersistentHashSet.EMPTY);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$recursive_QMARK_.invokeStatic(object3, object4);
    }
}

