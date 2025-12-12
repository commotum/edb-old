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
import datomic.index$filter_nohist_pairs$fn__15343;
import datomic.index$filter_nohist_pairs$fn__15345;

public final class index$filter_nohist_pairs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"fnext");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"nnext");

    public static Object invokeStatic(Object db2, Object data2) {
        LazySeq lazySeq;
        block13: {
            block12: {
                Object d;
                while (true) {
                    Object object;
                    Object n;
                    Object and__5236__auto__15352;
                    Object object2;
                    Object object3 = data2;
                    if (object3 == null || object3 == Boolean.FALSE) break block12;
                    d = ((IFn)const__0.getRawRoot()).invoke(data2);
                    Object nohist = ((Attribute)((IFn)index$filter_nohist_pairs.const__1.getRawRoot()).invoke((Object)db2, (Object)Integer.valueOf((int)((IDatum)d).getA()))).noHistory;
                    boolean or__5238__auto__15348 = ((IDatum)d).isAssertion();
                    if (or__5238__auto__15348) {
                        object2 = or__5238__auto__15348 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        Object object4 = nohist;
                        nohist = null;
                        object2 = ((IFn)const__2.getRawRoot()).invoke(object4);
                    }
                    if (object2 != null && object2 != Boolean.FALSE) {
                        Object object5 = data2;
                        data2 = null;
                        Object nd = ((IFn)const__3.getRawRoot()).invoke(object5);
                        nd = null;
                        d = null;
                        db2 = null;
                        lazySeq = new LazySeq((IFn)new index$filter_nohist_pairs$fn__15343(nd, d, db2));
                        break block13;
                    }
                    Object object6 = and__5236__auto__15352 = (n = ((IFn)const__4.getRawRoot()).invoke(data2));
                    if (object6 != null && object6 != Boolean.FALSE) {
                        boolean and__5236__auto__15351 = ((IDatum)n).isAssertion();
                        if (and__5236__auto__15351) {
                            boolean and__5236__auto__15350 = Util.equiv((long)((IDatum)d).getE(), (long)((IDatum)n).getE());
                            if (and__5236__auto__15350) {
                                boolean and__5236__auto__15349 = Util.equiv((long)((IDatum)d).getA(), (long)((IDatum)n).getA());
                                if (and__5236__auto__15349) {
                                    Object object7 = n;
                                    n = null;
                                    object = Numbers.isZero((long)((IFn.OOL)const__7.getRawRoot()).invokePrim(((IDatum)d).getV(), ((IDatum)object7).getV())) ? Boolean.TRUE : Boolean.FALSE;
                                } else {
                                    object = and__5236__auto__15349 ? Boolean.TRUE : Boolean.FALSE;
                                }
                            } else {
                                object = and__5236__auto__15350 ? Boolean.TRUE : Boolean.FALSE;
                            }
                        } else {
                            object = and__5236__auto__15351 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        object = and__5236__auto__15352;
                        and__5236__auto__15352 = null;
                    }
                    if (object == null || object == Boolean.FALSE) break;
                    Object object8 = db2;
                    db2 = null;
                    Object object9 = data2;
                    data2 = null;
                    data2 = ((IFn)const__8.getRawRoot()).invoke(object9);
                    db2 = object8;
                }
                d = null;
                data2 = null;
                db2 = null;
                lazySeq = new LazySeq((IFn)new index$filter_nohist_pairs$fn__15345(d, data2, db2));
                break block13;
            }
            lazySeq = null;
        }
        return lazySeq;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$filter_nohist_pairs.invokeStatic(object3, object4);
    }
}

