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
import datomic.db$find_aevt$fn__12973;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class db$find_aevt
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"find-aevt");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"filter-retractions");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__6 = RT.keyword(null, (String)"a");
    public static final Keyword const__7 = RT.keyword(null, (String)"e");
    public static final Keyword const__8 = RT.keyword(null, (String)"v");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__12 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object db2, Object a, Object e, Object v) {
        Object object;
        Object object2;
        Object it;
        Object and__5236__auto__12985;
        Object object3;
        Object attrid = ((IFn)const__1.getRawRoot()).invoke(db2, a);
        IFn iFn = (IFn)const__2.getRawRoot();
        IFn iFn2 = (IFn)const__3.getRawRoot();
        db$find_aevt$fn__12973 db$find_aevt$fn__12973 = new db$find_aevt$fn__12973(e, attrid, v);
        IDb iDb = (IDb)db2;
        if (Util.identical((Object)v, null)) {
            Object object4 = db2;
            db2 = null;
            Object object5 = a;
            a = null;
            object3 = ((IFn)const__5.getRawRoot()).invoke(object4, (Object)const__6, object5, (Object)const__7, e);
        } else {
            Object object6 = db2;
            db2 = null;
            Object object7 = a;
            a = null;
            object3 = ((IFn)const__5.getRawRoot()).invoke(object6, (Object)const__6, object7, (Object)const__7, e, (Object)const__8, v);
        }
        Object iter2 = iFn.invoke(iFn2.invoke((Object)db$find_aevt$fn__12973, (Object)iDb.seekAEVT((IDatum)object3)));
        Object object8 = and__5236__auto__12985 = (it = ((IFn)const__9.getRawRoot()).invoke(iter2));
        if (object8 != null && object8 != Boolean.FALSE) {
            boolean and__5236__auto__12984 = ((IDatum)it).isAssertion();
            if (and__5236__auto__12984) {
                Object object9 = attrid;
                attrid = null;
                boolean and__5236__auto__12983 = Util.equiv((Object)object9, (long)((IDatum)it).getA());
                if (and__5236__auto__12983) {
                    boolean and__5236__auto__12982;
                    boolean bl;
                    boolean or__5238__auto__12980 = Util.identical((Object)e, null);
                    if (or__5238__auto__12980) {
                        bl = or__5238__auto__12980;
                    } else {
                        e = null;
                        bl = and__5236__auto__12982 = Util.equiv((Object)e, (long)((IDatum)it).getE());
                    }
                    if (and__5236__auto__12982) {
                        boolean or__5238__auto__12981 = Util.identical((Object)v, null);
                        if (or__5238__auto__12981) {
                            object2 = or__5238__auto__12981 ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            Object object10 = v;
                            v = null;
                            Object object11 = it;
                            it = null;
                            object2 = Numbers.isZero((long)((IFn.OOL)const__12.getRawRoot()).invokePrim(object10, ((IDatum)object11).getV())) ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        object2 = and__5236__auto__12982 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    object2 = and__5236__auto__12983 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object2 = and__5236__auto__12984 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object2 = and__5236__auto__12985;
            and__5236__auto__12985 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = iter2;
            iter2 = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$find_aevt.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object db2, Object a, Object e) {
        Object object = db2;
        db2 = null;
        Object object2 = a;
        a = null;
        Object object3 = e;
        e = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, null);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$find_aevt.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object db2, Object a) {
        Object object = db2;
        db2 = null;
        Object object2 = a;
        a = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$find_aevt.invokeStatic(object3, object4);
    }
}

