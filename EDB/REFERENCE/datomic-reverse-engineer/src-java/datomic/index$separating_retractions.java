/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.LazySeq
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.impl.db.IDatum;
import datomic.index$separating_retractions$fn__15353;

public final class index$separating_retractions
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"fnext");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"nnext");

    public static Object invokeStatic(Object db2, Object retref, Object data2) {
        LazySeq lazySeq;
        block9: {
            while (true) {
                Object object;
                Object and__5236__auto__15359;
                Object object2 = data2;
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object d = ((IFn)const__0.getRawRoot()).invoke(data2);
                if (((IDatum)d).isAssertion()) {
                    Object object3 = data2;
                    data2 = null;
                    Object nd = ((IFn)const__1.getRawRoot()).invoke(object3);
                    nd = null;
                    retref = null;
                    db2 = null;
                    d = null;
                    lazySeq = new LazySeq((IFn)new index$separating_retractions$fn__15353(nd, retref, db2, d));
                    break block9;
                }
                Object n = ((IFn)const__2.getRawRoot()).invoke(data2);
                Object nohist = ((Attribute)((IFn)index$separating_retractions.const__3.getRawRoot()).invoke((Object)db2, (Object)Integer.valueOf((int)((IDatum)d).getA()))).noHistory;
                Object object4 = and__5236__auto__15359 = n;
                if (object4 != null && object4 != Boolean.FALSE) {
                    boolean and__5236__auto__15356;
                    boolean and__5236__auto__15357;
                    boolean and__5236__auto__15358 = ((IDatum)n).isAssertion();
                    object = and__5236__auto__15358 ? ((and__5236__auto__15357 = Util.equiv((long)((IDatum)d).getE(), (long)((IDatum)n).getE())) ? ((and__5236__auto__15356 = Util.equiv((long)((IDatum)d).getA(), (long)((IDatum)n).getA())) ? (Numbers.isZero((long)((IFn.OOL)const__6.getRawRoot()).invokePrim(((IDatum)d).getV(), ((IDatum)n).getV())) ? Boolean.TRUE : Boolean.FALSE) : (and__5236__auto__15356 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__15357 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__15358 ? Boolean.TRUE : Boolean.FALSE);
                } else {
                    object = and__5236__auto__15359;
                    and__5236__auto__15359 = null;
                }
                if (object != null && object != Boolean.FALSE) {
                    Object object5 = nohist;
                    nohist = null;
                    if (object5 != null && object5 != Boolean.FALSE) {
                    } else {
                        Object object6 = d;
                        d = null;
                        Object object7 = n;
                        n = null;
                        ((IFn)const__7.getRawRoot()).invoke(retref, const__8.getRawRoot(), object6, object7);
                    }
                    Object object8 = db2;
                    db2 = null;
                    Object object9 = retref;
                    retref = null;
                    Object object10 = data2;
                    data2 = null;
                    data2 = ((IFn)const__9.getRawRoot()).invoke(object10);
                    retref = object9;
                    db2 = object8;
                    continue;
                }
                Object object11 = nohist;
                nohist = null;
                if (object11 != null && object11 != Boolean.FALSE) {
                } else {
                    Object object12 = d;
                    d = null;
                    ((IFn)const__7.getRawRoot()).invoke(retref, const__8.getRawRoot(), object12);
                }
                Object object13 = db2;
                db2 = null;
                Object object14 = retref;
                retref = null;
                Object object15 = data2;
                data2 = null;
                data2 = ((IFn)const__1.getRawRoot()).invoke(object15);
                retref = object14;
                db2 = object13;
            }
            lazySeq = null;
        }
        return lazySeq;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$separating_retractions.invokeStatic(object4, object5, object6);
    }
}

