/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Datum;

public final class index$retract_assert_pair_QMARK_
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"false?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"true?");

    public static Object invokeStatic(Object d1, Object d2) {
        Object object;
        boolean and__5236__auto__15656 = Util.equiv((long)((Datum)d1).getE(), (long)((Datum)d2).getE());
        if (and__5236__auto__15656) {
            boolean and__5236__auto__15655 = Util.equiv((long)((Datum)d1).getA(), (long)((Datum)d2).getA());
            if (and__5236__auto__15655) {
                boolean and__5236__auto__15654 = Numbers.isZero((long)((IFn.OOL)const__2.getRawRoot()).invokePrim(((Datum)d1).getV(), ((Datum)d2).getV()));
                if (and__5236__auto__15654) {
                    Object and__5236__auto__15653;
                    Object object2 = d1;
                    d1 = null;
                    Object object3 = and__5236__auto__15653 = ((IFn)const__3.getRawRoot()).invoke((Object)(((Datum)object2).isAssertion() ? Boolean.TRUE : Boolean.FALSE));
                    if (object3 != null && object3 != Boolean.FALSE) {
                        Object object4 = d2;
                        d2 = null;
                        object = ((IFn)const__4.getRawRoot()).invoke((Object)(((Datum)object4).isAssertion() ? Boolean.TRUE : Boolean.FALSE));
                    } else {
                        object = and__5236__auto__15653;
                        and__5236__auto__15653 = null;
                    }
                } else {
                    object = and__5236__auto__15654 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object = and__5236__auto__15655 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object = and__5236__auto__15656 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$retract_assert_pair_QMARK_.invokeStatic(object3, object4);
    }
}

