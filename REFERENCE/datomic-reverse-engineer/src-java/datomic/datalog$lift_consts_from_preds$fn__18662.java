/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
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
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$lift_consts_from_preds$fn__18662$fn__18673;

public final class datalog$lift_consts_from_preds$fn__18662
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"not=");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"not-join");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"variable-or-blank?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"cons");

    public Object invoke(Object p__18660, Object p__18661) {
        IPersistentVector iPersistentVector;
        Object object;
        Object and__5236__auto__18682;
        Object object2 = p__18660;
        p__18660 = null;
        Object vec__18663 = object2;
        Object m = RT.nth((Object)vec__18663, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__18663;
        vec__18663 = null;
        Object cs = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = p__18661;
        p__18661 = null;
        Object vec__18666 = object4;
        Object seq__18667 = ((IFn)const__3.getRawRoot()).invoke(vec__18666);
        Object first__18668 = ((IFn)const__4.getRawRoot()).invoke(seq__18667);
        Object object5 = seq__18667;
        seq__18667 = null;
        Object seq__186672 = ((IFn)const__5.getRawRoot()).invoke(object5);
        Object object6 = first__18668;
        first__18668 = null;
        Object pred2 = object6;
        Object object7 = seq__186672;
        seq__186672 = null;
        Object args = object7;
        Object object8 = vec__18666;
        vec__18666 = null;
        Object c = object8;
        Object object9 = and__5236__auto__18682 = ((IFn)const__6.getRawRoot()).invoke(pred2);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object and__5236__auto__18681;
            Object object10 = and__5236__auto__18681 = ((IFn)const__7.getRawRoot()).invoke((Object)const__8, pred2);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object and__5236__auto__18680;
                Object object11 = and__5236__auto__18680 = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(pred2));
                if (object11 != null && object11 != Boolean.FALSE) {
                    Object and__5236__auto__18679;
                    Object object12 = and__5236__auto__18679 = ((IFn)const__9.getRawRoot()).invoke((Object)(((String)((IFn)const__11.getRawRoot()).invoke(pred2)).startsWith("$") ? Boolean.TRUE : Boolean.FALSE));
                    if (object12 != null && object12 != Boolean.FALSE) {
                        object = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__10.getRawRoot(), args));
                    } else {
                        object = and__5236__auto__18679;
                        and__5236__auto__18679 = null;
                    }
                } else {
                    object = and__5236__auto__18680;
                    and__5236__auto__18680 = null;
                }
            } else {
                object = and__5236__auto__18681;
                and__5236__auto__18681 = null;
            }
        } else {
            object = and__5236__auto__18682;
            and__5236__auto__18682 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object13 = m;
            m = null;
            Object object14 = args;
            args = null;
            Object vec__18669 = ((IFn)const__13.getRawRoot()).invoke((Object)new datalog$lift_consts_from_preds$fn__18662$fn__18673(), (Object)Tuple.create((Object)object13, (Object)PersistentVector.EMPTY), object14);
            Object m2 = RT.nth((Object)vec__18669, (int)RT.uncheckedIntCast((long)0L), null);
            Object object15 = vec__18669;
            vec__18669 = null;
            Object args2 = RT.nth((Object)object15, (int)RT.uncheckedIntCast((long)1L), null);
            Object object16 = m2;
            m2 = null;
            Object object17 = cs;
            cs = null;
            Object object18 = pred2;
            pred2 = null;
            Object object19 = args2;
            args2 = null;
            iPersistentVector = Tuple.create((Object)object16, (Object)((IFn)const__14.getRawRoot()).invoke(object17, ((IFn)const__15.getRawRoot()).invoke(object18, object19)));
        } else {
            Object object20 = m;
            m = null;
            Object object21 = cs;
            cs = null;
            Object object22 = c;
            c = null;
            iPersistentVector = Tuple.create((Object)object20, (Object)((IFn)const__14.getRawRoot()).invoke(object21, object22));
        }
        return iPersistentVector;
    }
}

