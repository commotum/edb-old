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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.btset.IDataSet;
import datomic.db$ident_setting_datoms$fn__13574;
import datomic.db$ident_setting_datoms$fn__13576;
import datomic.db.IndexSet;
import datomic.iter.Iter;

public final class db$ident_setting_datoms
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"sort-by");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__2 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"filter");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"assertion?");
    public static final Var const__5 = RT.var((String)"datomic.iter", (String)"merge-iters");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"aevt-cmp");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__8 = RT.keyword(null, (String)"a");
    public static final Object const__9 = 10L;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object db2, Object index2) {
        Iter iter2;
        Object object;
        Object and__5236__auto__13580;
        Iter iter3;
        Object object2;
        Object and__5236__auto__13579;
        IFn iFn = (IFn)const__0.getRawRoot();
        db$ident_setting_datoms$fn__13574 db$ident_setting_datoms$fn__13574 = new db$ident_setting_datoms$fn__13574();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        IFn iFn3 = (IFn)const__2.getRawRoot();
        db$ident_setting_datoms$fn__13576 db$ident_setting_datoms$fn__13576 = new db$ident_setting_datoms$fn__13576();
        IFn iFn4 = (IFn)const__3.getRawRoot();
        Object object3 = const__4.getRawRoot();
        IFn iFn5 = (IFn)const__5.getRawRoot();
        Object object4 = const__6.getRawRoot();
        Object object5 = index2;
        index2 = null;
        Iter iter4 = ((IDataSet)((IndexSet)object5).aevt).seek(((IFn)const__7.getRawRoot()).invoke(db2, (Object)const__8, const__9));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = db2;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        Object object8 = and__5236__auto__13579 = object7;
        if (object8 != null && object8 != Boolean.FALSE) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object9 = db2;
            Object object10 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            object2 = ((IndexSet)object10).aevt;
        } else {
            object2 = and__5236__auto__13579;
            and__5236__auto__13579 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object11 = db2;
            Object object12 = iLookupThunk3.get(object11);
            if (iLookupThunk3 == object12) {
                __thunk__2__ = __site__2__.fault(object11);
                object12 = __thunk__2__.get(object11);
            }
            iter3 = ((IDataSet)((IndexSet)object12).aevt).seek(((IFn)const__7.getRawRoot()).invoke(db2, (Object)const__8, const__9));
        } else {
            iter3 = null;
        }
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object13 = db2;
        Object object14 = iLookupThunk4.get(object13);
        if (iLookupThunk4 == object14) {
            __thunk__3__ = __site__3__.fault(object13);
            object14 = __thunk__3__.get(object13);
        }
        Object object15 = and__5236__auto__13580 = object14;
        if (object15 != null && object15 != Boolean.FALSE) {
            ILookupThunk iLookupThunk5 = __thunk__4__;
            Object object16 = db2;
            Object object17 = iLookupThunk5.get(object16);
            if (iLookupThunk5 == object17) {
                __thunk__4__ = __site__4__.fault(object16);
                object17 = __thunk__4__.get(object16);
            }
            object = ((IndexSet)object17).aevt;
        } else {
            object = and__5236__auto__13580;
            Object var2_2 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            ILookupThunk iLookupThunk6 = __thunk__5__;
            Object object18 = db2;
            Object object19 = iLookupThunk6.get(object18);
            if (iLookupThunk6 == object19) {
                __thunk__5__ = __site__5__.fault(object18);
                object19 = __thunk__5__.get(object18);
            }
            Object object20 = db2;
            db2 = null;
            iter2 = ((IDataSet)((IndexSet)object19).aevt).seek(((IFn)const__7.getRawRoot()).invoke(object20, (Object)const__8, const__9));
        } else {
            iter2 = null;
        }
        return iFn.invoke((Object)db$ident_setting_datoms$fn__13574, iFn2.invoke(iFn3.invoke((Object)db$ident_setting_datoms$fn__13576, iFn4.invoke(object3, iFn5.invoke(object4, (Object)iter4, (Object)iter3, iter2)))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$ident_setting_datoms.invokeStatic(object3, object4);
    }
}

