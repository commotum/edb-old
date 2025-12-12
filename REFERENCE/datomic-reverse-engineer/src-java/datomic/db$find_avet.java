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
import datomic.db$find_avet$fn__12964;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class db$find_avet
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"find-avet");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"filter-retractions");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__5 = RT.keyword(null, (String)"a");
    public static final Keyword const__6 = RT.keyword(null, (String)"v");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__11 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object db2, Object a, Object v) {
        Object object;
        Object object2;
        Object it;
        Object and__5236__auto__12972;
        Object attrid = ((IFn)const__1.getRawRoot()).invoke(db2, a);
        IDb iDb = (IDb)db2;
        Object object3 = db2;
        db2 = null;
        Object object4 = a;
        a = null;
        Object iter2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)new db$find_avet$fn__12964(v, attrid), (Object)iDb.seekAVET((IDatum)((IFn)const__4.getRawRoot()).invoke(object3, (Object)const__5, object4, (Object)const__6, v))));
        Object object5 = and__5236__auto__12972 = (it = ((IFn)const__7.getRawRoot()).invoke(iter2));
        if (object5 != null && object5 != Boolean.FALSE) {
            boolean and__5236__auto__12971 = ((IDatum)it).isAssertion();
            if (and__5236__auto__12971) {
                Object object6 = attrid;
                attrid = null;
                boolean and__5236__auto__12970 = Util.equiv((Object)object6, (long)((IDatum)it).getA());
                if (and__5236__auto__12970) {
                    boolean or__5238__auto__12969 = Util.identical((Object)v, null);
                    if (or__5238__auto__12969) {
                        object2 = or__5238__auto__12969 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        Object object7 = v;
                        v = null;
                        Object object8 = it;
                        it = null;
                        object2 = Numbers.isZero((long)((IFn.OOL)const__11.getRawRoot()).invokePrim(object7, ((IDatum)object8).getV())) ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    object2 = and__5236__auto__12970 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object2 = and__5236__auto__12971 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object2 = and__5236__auto__12972;
            and__5236__auto__12972 = null;
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
        return db$find_avet.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object db2, Object a) {
        Object object = db2;
        db2 = null;
        Object object2 = a;
        a = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$find_avet.invokeStatic(object3, object4);
    }
}

