/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class fulltext$separate_history
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"conj!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"fnext");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"nnext");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__10 = RT.var((String)"datomic.fulltext", (String)"find-matching-assertion");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"persistent!");

    public static Object invokeStatic(Object db2, Object data2, Object history2) {
        Object ds = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
        Object object = data2;
        data2 = null;
        Object data3 = object;
        Object object2 = history2;
        history2 = null;
        Object history3 = object2;
        while (true) {
            Object object3;
            Object temp__5455__auto__14670;
            Object object4;
            Object n;
            Object and__5236__auto__14669;
            Object object5 = data3;
            if (object5 == null || object5 == Boolean.FALSE) break;
            Object d = ((IFn)const__1.getRawRoot()).invoke(data3);
            if (((IDatum)d).isAssertion()) {
                Object object6 = ds;
                ds = null;
                Object object7 = d;
                d = null;
                Object object8 = data3;
                data3 = null;
                Object object9 = history3;
                history3 = null;
                history3 = object9;
                data3 = ((IFn)const__3.getRawRoot()).invoke(object8);
                ds = ((IFn)const__2.getRawRoot()).invoke(object6, object7);
                continue;
            }
            Object object10 = and__5236__auto__14669 = (n = ((IFn)const__4.getRawRoot()).invoke(data3));
            if (object10 != null && object10 != Boolean.FALSE) {
                boolean and__5236__auto__14666;
                boolean and__5236__auto__14667;
                boolean and__5236__auto__14668 = ((IDatum)n).isAssertion();
                object4 = and__5236__auto__14668 ? ((and__5236__auto__14667 = Util.equiv((long)((IDatum)d).getE(), (long)((IDatum)n).getE())) ? ((and__5236__auto__14666 = Util.equiv((long)((IDatum)d).getA(), (long)((IDatum)n).getA())) ? (Numbers.isZero((long)((IFn.OOL)const__7.getRawRoot()).invokePrim(((IDatum)d).getV(), ((IDatum)n).getV())) ? Boolean.TRUE : Boolean.FALSE) : (and__5236__auto__14666 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__14667 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__14668 ? Boolean.TRUE : Boolean.FALSE);
            } else {
                object4 = and__5236__auto__14669;
                and__5236__auto__14669 = null;
            }
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object11 = ds;
                ds = null;
                Object object12 = data3;
                data3 = null;
                Object object13 = history3;
                history3 = null;
                Object object14 = n;
                n = null;
                history3 = ((IFn)const__9.getRawRoot()).invoke(object13, object14);
                data3 = ((IFn)const__8.getRawRoot()).invoke(object12);
                ds = object11;
                continue;
            }
            Object object15 = ds;
            ds = null;
            Object object16 = data3;
            data3 = null;
            Object object17 = ((IFn)const__3.getRawRoot()).invoke(object16);
            Object object18 = d;
            d = null;
            Object object19 = temp__5455__auto__14670 = ((IFn)const__10.getRawRoot()).invoke(db2, object18);
            if (object19 != null && object19 != Boolean.FALSE) {
                Object object20 = temp__5455__auto__14670;
                temp__5455__auto__14670 = null;
                Object match = object20;
                Object object21 = history3;
                history3 = null;
                Object object22 = match;
                match = null;
                object3 = ((IFn)const__9.getRawRoot()).invoke(object21, object22);
            } else {
                object3 = null;
            }
            history3 = object3;
            data3 = object17;
            ds = object15;
        }
        Object object23 = ds;
        ds = null;
        Object object24 = history3;
        history3 = null;
        return Tuple.create((Object)((IFn)const__11.getRawRoot()).invoke(object23), (Object)object24);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fulltext$separate_history.invokeStatic(object4, object5, object6);
    }
}

