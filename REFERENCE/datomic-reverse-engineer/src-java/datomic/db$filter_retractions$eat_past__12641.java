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
import datomic.impl.db.IDatum;

public final class db$filter_retractions$eat_past__12641
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"inext");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"compare");

    public Object invoke(Object d, Object i) {
        Object object;
        block10: {
            block9: {
                Object j;
                while (true) {
                    Object object2;
                    Object n;
                    Object and__5236__auto__12646;
                    Object object3 = i;
                    if (object3 == null || object3 == Boolean.FALSE) break block9;
                    Object object4 = i;
                    i = null;
                    j = ((IFn)const__0.getRawRoot()).invoke(object4);
                    Object object5 = and__5236__auto__12646 = (n = ((IFn)const__1.getRawRoot()).invoke(j));
                    if (object5 != null && object5 != Boolean.FALSE) {
                        boolean and__5236__auto__12645 = Util.equiv((long)((IDatum)d).getE(), (long)((IDatum)n).getE());
                        if (and__5236__auto__12645) {
                            boolean and__5236__auto__12644 = Util.equiv((long)((IDatum)d).getA(), (long)((IDatum)n).getA());
                            if (and__5236__auto__12644) {
                                boolean and__5236__auto__12643 = Numbers.isZero((long)((IFn.OOL)const__4.getRawRoot()).invokePrim(((IDatum)d).getV(), ((IDatum)n).getV()));
                                if (and__5236__auto__12643) {
                                    Object object6 = n;
                                    n = null;
                                    object2 = Numbers.lte((long)((IDatum)object6).getT(), (long)((IDatum)d).getT()) ? Boolean.TRUE : Boolean.FALSE;
                                } else {
                                    object2 = and__5236__auto__12643 ? Boolean.TRUE : Boolean.FALSE;
                                }
                            } else {
                                object2 = and__5236__auto__12644 ? Boolean.TRUE : Boolean.FALSE;
                            }
                        } else {
                            object2 = and__5236__auto__12645 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        object2 = and__5236__auto__12646;
                        and__5236__auto__12646 = null;
                    }
                    if (object2 == null || object2 == Boolean.FALSE) break;
                    Object object7 = d;
                    d = null;
                    Object object8 = j;
                    j = null;
                    i = object8;
                    d = object7;
                }
                object = j;
                Object var3_3 = null;
                break block10;
            }
            object = null;
        }
        return object;
    }
}

