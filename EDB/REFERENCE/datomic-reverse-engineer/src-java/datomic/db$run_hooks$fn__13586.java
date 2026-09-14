/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
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
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset.IDataSet;
import datomic.db.IndexSet;
import datomic.impl.db.IDatum;
import datomic.iter.Iter;

public final class db$run_hooks$fn__13586
extends AFunction {
    Object index;
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"filter");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"assertion?");
    public static final Var const__5 = RT.var((String)"datomic.iter", (String)"merge-iters");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"aevt-cmp");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__8 = RT.keyword(null, (String)"a");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__12 = RT.var((String)"datomic.iter", (String)"inext");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public db$run_hooks$fn__13586(Object object) {
        this.index = object;
    }

    public Object invoke(Object db2, Object p__13585) {
        Iter iter2;
        Object object;
        Object and__5236__auto__13591;
        Object object2 = p__13585;
        p__13585 = null;
        Object vec__13587 = object2;
        Object attrid = RT.nth((Object)vec__13587, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__13587;
        vec__13587 = null;
        Object f = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = db2;
        db2 = null;
        Object db3 = object4;
        IFn iFn = (IFn)const__3.getRawRoot();
        Object object5 = const__4.getRawRoot();
        IFn iFn2 = (IFn)const__5.getRawRoot();
        Object object6 = const__6.getRawRoot();
        Iter iter3 = ((IDataSet)((IndexSet)this.index).aevt).seek(((IFn)const__7.getRawRoot()).invoke(db3, (Object)const__8, attrid));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = db3;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        Object object9 = and__5236__auto__13591 = object8;
        if (object9 != null && object9 != Boolean.FALSE) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object10 = db3;
            Object object11 = iLookupThunk2.get(object10);
            if (iLookupThunk2 == object11) {
                __thunk__1__ = __site__1__.fault(object10);
                object11 = __thunk__1__.get(object10);
            }
            object = ((IndexSet)object11).aevt;
        } else {
            object = and__5236__auto__13591;
            and__5236__auto__13591 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object12 = db3;
            Object object13 = iLookupThunk3.get(object12);
            if (iLookupThunk3 == object13) {
                __thunk__2__ = __site__2__.fault(object12);
                object13 = __thunk__2__.get(object12);
            }
            iter2 = ((IDataSet)((IndexSet)object13).aevt).seek(((IFn)const__7.getRawRoot()).invoke(db3, (Object)const__8, attrid));
        } else {
            iter2 = null;
        }
        Object iter4 = iFn.invoke(object5, iFn2.invoke(object6, (Object)iter3, iter2));
        while (true) {
            Object object14;
            Object and__5236__auto__13592;
            Object object15 = and__5236__auto__13592 = iter4;
            if (object15 != null && object15 != Boolean.FALSE) {
                object14 = Util.equiv((Object)attrid, (long)((IDatum)((IFn)const__11.getRawRoot()).invoke(iter4)).getA()) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object14 = and__5236__auto__13592;
                and__5236__auto__13592 = null;
            }
            if (object14 == null || object14 == Boolean.FALSE) break;
            Object object16 = db3;
            db3 = null;
            Object object17 = ((IFn)f).invoke(null, object16, ((IFn)const__11.getRawRoot()).invoke(iter4), (Object)Boolean.FALSE);
            Object object18 = iter4;
            iter4 = null;
            iter4 = ((IFn)const__12.getRawRoot()).invoke(object18);
            db3 = object17;
        }
        Object object19 = db3;
        db3 = null;
        return object19;
    }
}

