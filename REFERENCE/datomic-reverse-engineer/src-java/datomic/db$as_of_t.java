/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import java.util.Date;

public final class db$as_of_t
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword((String)"db", (String)"txInstant");
    public static final Keyword const__6 = RT.keyword(null, (String)"v");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"eid->eidx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"nextT"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object t_or_date) {
        Object object;
        if (t_or_date instanceof Date) {
            Object object2;
            Object d;
            Object and__5236__auto__13262;
            Object object3 = and__5236__auto__13262 = (d = ((IFn)const__2.getRawRoot()).invoke((Object)((IDb)db2).seekAVET((IDatum)((IFn)const__3.getRawRoot()).invoke(db2, (Object)const__4, (Object)const__5, (Object)const__6, t_or_date))));
            if (object3 != null && object3 != Boolean.FALSE) {
                object2 = Util.equiv((Object)((IFn)const__8.getRawRoot()).invoke(db2, (Object)const__5), (long)((IDatum)d).getA()) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__13262;
                Object var3_3 = null;
            }
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object4 = t_or_date;
                t_or_date = null;
                if (Util.equiv((Object)((IDatum)d).getV(), (Object)object4)) {
                    Object object5 = d;
                    d = null;
                    object = Numbers.num((long)((IDatum)object5).getT());
                } else {
                    Object object6 = d;
                    d = null;
                    object = Numbers.num((long)Numbers.unchecked_dec((long)((IDatum)object6).getT()));
                }
            } else {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object7 = db2;
                db2 = null;
                object = iLookupThunk.get(object7);
                if (iLookupThunk == object) {
                    __thunk__0__ = __site__0__.fault(object7);
                    object = __thunk__0__.get(object7);
                }
            }
        } else {
            Object object8 = t_or_date;
            t_or_date = null;
            object = Numbers.num((long)((IFn.LL)const__11.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object8))));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$as_of_t.invokeStatic(object3, object4);
    }
}

