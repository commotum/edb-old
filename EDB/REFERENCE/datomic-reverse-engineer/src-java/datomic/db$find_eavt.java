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
import datomic.db$find_eavt$fn__12986;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class db$find_eavt
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"find-eavt");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"filter-retractions");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__6 = RT.keyword(null, (String)"e");
    public static final Keyword const__7 = RT.keyword(null, (String)"a");
    public static final Keyword const__8 = RT.keyword(null, (String)"v");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__12 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object db2, Object e, Object a, Object v) {
        Object object;
        Object object2;
        Object it;
        Object and__5236__auto__13001;
        Object object3;
        Object object4;
        Object and__5236__auto__12994;
        Object object5 = and__5236__auto__12994 = a;
        if (object5 != null && object5 != Boolean.FALSE) {
            object4 = ((IFn)const__1.getRawRoot()).invoke(db2, a);
        } else {
            object4 = and__5236__auto__12994;
            and__5236__auto__12994 = null;
        }
        Object attrid = object4;
        IFn iFn = (IFn)const__2.getRawRoot();
        IFn iFn2 = (IFn)const__3.getRawRoot();
        db$find_eavt$fn__12986 db$find_eavt$fn__12986 = new db$find_eavt$fn__12986(attrid, a, v, e);
        IDb iDb = (IDb)db2;
        if (Util.identical((Object)v, null)) {
            Object object6 = db2;
            db2 = null;
            object3 = ((IFn)const__5.getRawRoot()).invoke(object6, (Object)const__6, e, (Object)const__7, a);
        } else {
            Object object7 = db2;
            db2 = null;
            object3 = ((IFn)const__5.getRawRoot()).invoke(object7, (Object)const__6, e, (Object)const__7, a, (Object)const__8, v);
        }
        Object iter2 = iFn.invoke(iFn2.invoke((Object)db$find_eavt$fn__12986, (Object)iDb.seekEAVT((IDatum)object3)));
        Object object8 = and__5236__auto__13001 = (it = ((IFn)const__9.getRawRoot()).invoke(iter2));
        if (object8 != null && object8 != Boolean.FALSE) {
            boolean and__5236__auto__13000 = ((IDatum)it).isAssertion();
            if (and__5236__auto__13000) {
                boolean and__5236__auto__12999;
                boolean bl;
                boolean or__5238__auto__12995 = Util.identical((Object)e, null);
                if (or__5238__auto__12995) {
                    bl = or__5238__auto__12995;
                } else {
                    e = null;
                    bl = and__5236__auto__12999 = Util.equiv((Object)e, (long)((IDatum)it).getE());
                }
                if (and__5236__auto__12999) {
                    boolean and__5236__auto__12998;
                    boolean bl2;
                    Object object9 = a;
                    a = null;
                    boolean or__5238__auto__12996 = Util.identical((Object)object9, null);
                    if (or__5238__auto__12996) {
                        bl2 = or__5238__auto__12996;
                    } else {
                        attrid = null;
                        bl2 = and__5236__auto__12998 = Util.equiv((Object)attrid, (long)((IDatum)it).getA());
                    }
                    if (and__5236__auto__12998) {
                        boolean or__5238__auto__12997 = Util.identical((Object)v, null);
                        if (or__5238__auto__12997) {
                            object2 = or__5238__auto__12997 ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            Object object10 = v;
                            v = null;
                            Object object11 = it;
                            it = null;
                            object2 = Numbers.isZero((long)((IFn.OOL)const__12.getRawRoot()).invokePrim(object10, ((IDatum)object11).getV())) ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        object2 = and__5236__auto__12998 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    object2 = and__5236__auto__12999 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object2 = and__5236__auto__13000 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object2 = and__5236__auto__13001;
            and__5236__auto__13001 = null;
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
        return db$find_eavt.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object db2, Object e, Object a) {
        Object object = db2;
        db2 = null;
        Object object2 = e;
        e = null;
        Object object3 = a;
        a = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, null);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$find_eavt.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object db2, Object e) {
        Object object = db2;
        db2 = null;
        Object object2 = e;
        e = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$find_eavt.invokeStatic(object3, object4);
    }
}

