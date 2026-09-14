/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$find_raet$fn__13002;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class db$find_raet
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"find-raet");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"filter-retractions");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__6 = RT.keyword(null, (String)"v");
    public static final Keyword const__7 = RT.keyword(null, (String)"a");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"dget");

    public static Object invokeStatic(Object db2, Object r, Object a) {
        Object object;
        Object object2;
        Object it;
        Object and__5236__auto__13012;
        Object object3;
        Object and__5236__auto__13008;
        Object rid;
        Object object4;
        Object and__5236__auto__13007;
        Object object5 = and__5236__auto__13007 = r;
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = r;
            r = null;
            object4 = ((IFn)const__1.getRawRoot()).invoke(db2, object6);
        } else {
            object4 = and__5236__auto__13007;
            rid = null;
        }
        rid = object4;
        Object object7 = and__5236__auto__13008 = a;
        if (object7 != null && object7 != Boolean.FALSE) {
            object3 = ((IFn)const__2.getRawRoot()).invoke(db2, a);
        } else {
            object3 = and__5236__auto__13008;
            and__5236__auto__13008 = null;
        }
        Object attrid = object3;
        IDb iDb = (IDb)db2;
        Object object8 = db2;
        db2 = null;
        Object iter2 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)new db$find_raet$fn__13002(rid, attrid, a), (Object)iDb.seekRAET((IDatum)((IFn)const__5.getRawRoot()).invoke(object8, (Object)const__6, rid, (Object)const__7, a))));
        Object object9 = and__5236__auto__13012 = (it = ((IFn)const__8.getRawRoot()).invoke(iter2));
        if (object9 != null && object9 != Boolean.FALSE) {
            boolean and__5236__auto__13011 = ((IDatum)it).isAssertion();
            if (and__5236__auto__13011) {
                Object object10 = rid;
                rid = null;
                boolean and__5236__auto__13010 = Util.equiv((Object)object10, (Object)((IDatum)it).getV());
                if (and__5236__auto__13010) {
                    Object object11 = a;
                    a = null;
                    boolean or__5238__auto__13009 = Util.identical((Object)object11, null);
                    if (or__5238__auto__13009) {
                        object2 = or__5238__auto__13009 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        Object object12 = attrid;
                        attrid = null;
                        Object object13 = it;
                        it = null;
                        object2 = Util.equiv((Object)object12, (long)((IDatum)object13).getA()) ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    object2 = and__5236__auto__13010 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object2 = and__5236__auto__13011 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object2 = and__5236__auto__13012;
            and__5236__auto__13012 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = iter2;
            iter2 = null;
        } else {
            object = null;
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
        return db$find_raet.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object db2, Object r) {
        Object object = db2;
        db2 = null;
        Object object2 = r;
        r = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$find_raet.invokeStatic(object3, object4);
    }
}

