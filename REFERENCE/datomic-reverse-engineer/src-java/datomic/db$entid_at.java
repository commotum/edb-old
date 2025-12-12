/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$LLL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import java.util.Arrays;
import java.util.Date;

public final class db$entid_at
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"partbits");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__5 = RT.keyword(null, (String)"a");
    public static final Object const__6 = 50L;
    public static final Keyword const__7 = RT.keyword(null, (String)"v");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__15 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"or"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"zero?"), Symbol.intern(null, (String)"tpart")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 27})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), Symbol.intern(null, (String)"PART_TX"), Symbol.intern(null, (String)"tpart")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 41}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 23}));
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"make-eid");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"nextT"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object partition, Object t_or_date) {
        Object object;
        Object object2 = partition;
        partition = null;
        Object partition2 = ((IFn)const__0.getRawRoot()).invoke(db2, object2);
        if (t_or_date instanceof Date) {
            Object object3;
            Object d;
            Object and__5236__auto__13266;
            Object object4 = t_or_date;
            t_or_date = null;
            Object object5 = and__5236__auto__13266 = (d = ((IFn)const__3.getRawRoot()).invoke((Object)((IDb)db2).seekAVET((IDatum)((IFn)const__4.getRawRoot()).invoke(db2, (Object)const__5, const__6, (Object)const__7, object4))));
            if (object5 != null && object5 != Boolean.FALSE) {
                object3 = Util.equiv((long)50L, (long)((IDatum)d).getA()) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object3 = and__5236__auto__13266;
                and__5236__auto__13266 = null;
            }
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object6 = d;
                d = null;
                object = Numbers.num((long)((IDatum)object6).getT());
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
            long tpart = ((IFn.LL)const__10.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)t_or_date)));
            boolean or__5238__auto__13267 = Numbers.isZero((long)tpart);
            if (!(or__5238__auto__13267 ? or__5238__auto__13267 : Util.equiv((long)3L, (long)tpart))) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__13.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"t must be raw t or txid", (Object)"\n", ((IFn)const__14.getRawRoot()).invoke(const__15))));
            }
            Object object8 = t_or_date;
            t_or_date = null;
            object = Numbers.num((long)((IFn.LL)const__16.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object8))));
        }
        Number t = object;
        Object object9 = partition2;
        partition2 = null;
        Number number = t;
        t = null;
        return Numbers.num((long)((IFn.LLL)const__17.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object9)), RT.uncheckedLongCast((Object)number)));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$entid_at.invokeStatic(object4, object5, object6);
    }
}

