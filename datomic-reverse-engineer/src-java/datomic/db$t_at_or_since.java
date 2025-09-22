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

public final class db$t_at_or_since
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword((String)"db", (String)"txInstant");
    public static final Keyword const__6 = RT.keyword(null, (String)"v");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"eid->eidx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"nextT"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object t_or_date) {
        Object object;
        if (t_or_date instanceof Date) {
            Object object2;
            Object d;
            Object and__5236__auto__13264;
            Object object3 = t_or_date;
            t_or_date = null;
            Object object4 = and__5236__auto__13264 = (d = ((IFn)const__2.getRawRoot()).invoke((Object)((IDb)db2).seekAVET((IDatum)((IFn)const__3.getRawRoot()).invoke(db2, (Object)const__4, (Object)const__5, (Object)const__6, object3))));
            if (object4 != null && object4 != Boolean.FALSE) {
                object2 = Util.equiv((Object)((IFn)const__8.getRawRoot()).invoke(db2, (Object)const__5), (long)((IDatum)d).getA()) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__13264;
                Object var3_3 = null;
            }
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object5 = d;
                d = null;
                object = Numbers.num((long)((IDatum)object5).getT());
            } else {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object6 = db2;
                db2 = null;
                object = iLookupThunk.get(object6);
                if (iLookupThunk == object) {
                    __thunk__0__ = __site__0__.fault(object6);
                    object = __thunk__0__.get(object6);
                }
            }
        } else {
            Object object7 = t_or_date;
            t_or_date = null;
            object = Numbers.num((long)((IFn.LL)const__10.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object7))));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$t_at_or_since.invokeStatic(object3, object4);
    }
}

