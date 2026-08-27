/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.IDb;
import datomic.fulltext$find_matching_assertion$fn__14659;
import datomic.impl.db.IDatum;

public final class fulltext$find_matching_assertion
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"filter");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__2 = RT.keyword(null, (String)"a");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"v");
    public static final Keyword const__5 = RT.keyword(null, (String)"asserting");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__9 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object object2;
        Object it;
        Object and__5236__auto__14664;
        Object iter2;
        IDb iDb = (IDb)db2;
        Object object3 = db2;
        db2 = null;
        Object object4 = iter2 = ((IFn)const__0.getRawRoot()).invoke((Object)new fulltext$find_matching_assertion$fn__14659(), (Object)iDb.seekAEVT((IDatum)((IFn)const__1.getRawRoot()).invoke(object3, (Object)const__2, (Object)((IDatum)d).getA(), (Object)const__3, (Object)Numbers.num((long)((IDatum)d).getE()), (Object)const__4, ((IDatum)d).getV(), (Object)const__5, (Object)Boolean.TRUE)));
        iter2 = null;
        Object object5 = and__5236__auto__14664 = (it = ((IFn)const__6.getRawRoot()).invoke(object4));
        if (object5 != null && object5 != Boolean.FALSE) {
            boolean and__5236__auto__14663 = Util.equiv((long)((IDatum)d).getE(), (long)((IDatum)it).getE());
            if (and__5236__auto__14663) {
                boolean and__5236__auto__14662 = Util.equiv((long)((IDatum)d).getA(), (long)((IDatum)it).getA());
                if (and__5236__auto__14662) {
                    Object object6 = d;
                    d = null;
                    object2 = Numbers.isZero((long)((IFn.OOL)const__9.getRawRoot()).invokePrim(((IDatum)object6).getV(), ((IDatum)it).getV())) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object2 = and__5236__auto__14662 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object2 = and__5236__auto__14663 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object2 = and__5236__auto__14664;
            and__5236__auto__14664 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = it;
            it = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext$find_matching_assertion.invokeStatic(object3, object4);
    }
}

