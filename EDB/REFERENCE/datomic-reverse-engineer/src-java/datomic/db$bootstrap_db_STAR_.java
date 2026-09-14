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
import datomic.db$bootstrap_db_STAR_$ad__14149;
import datomic.db$bootstrap_db_STAR_$fn__14154;
import datomic.db$bootstrap_db_STAR_$fn__14156;
import datomic.db$bootstrap_db_STAR_$fn__14158;
import datomic.db.Db;
import java.util.Date;

public final class db$bootstrap_db_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"bootstrap-db*");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"init-db");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"bootstrap-data");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"add-fulltext");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"bootstrap-txes");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"run-hooks");
    public static final Keyword const__9 = RT.keyword(null, (String)"memidx");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"finish-init");
    public static final Keyword const__11 = RT.keyword(null, (String)"avet");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__14 = RT.keyword(null, (String)"raet");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__16 = RT.keyword(null, (String)"nextT");
    public static final Object const__17 = 1000L;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"raet"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object id) {
        Object db2;
        Date epoch;
        db$bootstrap_db_STAR_$ad__14149 ad;
        Object object = id;
        id = null;
        Object db3 = ((IFn)const__1.getRawRoot()).invoke(object);
        db$bootstrap_db_STAR_$ad__14149 db$bootstrap_db_STAR_$ad__14149 = ad = new db$bootstrap_db_STAR_$ad__14149(db3);
        ad = null;
        Object data2 = ((IFn)const__2.getRawRoot()).invoke((Object)db$bootstrap_db_STAR_$ad__14149, const__3.getRawRoot());
        Object object2 = db3;
        db3 = null;
        Object object3 = ((Db)object2).acceptDataCheck(data2, Boolean.FALSE);
        Object object4 = data2;
        data2 = null;
        Object db4 = ((IFn)const__4.getRawRoot()).invoke(object3, object4);
        Date date = epoch = new Date(0L);
        epoch = null;
        Object object5 = db4;
        db4 = null;
        Object db5 = ((IFn)const__6.getRawRoot()).invoke((Object)new db$bootstrap_db_STAR_$fn__14154(date), object5, const__7.getRawRoot());
        IFn iFn = (IFn)const__8.getRawRoot();
        Object object6 = db5;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = db5;
        db5 = null;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        Object object9 = db2 = iFn.invoke(object6, object8);
        db2 = null;
        Object db6 = ((IFn)const__10.getRawRoot()).invoke(object9);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object10 = db6;
        Object object11 = iLookupThunk2.get(object10);
        if (iLookupThunk2 == object11) {
            __thunk__1__ = __site__1__.fault(object10);
            object11 = __thunk__1__.get(object10);
        }
        Object midx = object11;
        IFn iFn2 = (IFn)const__6.getRawRoot();
        db$bootstrap_db_STAR_$fn__14156 db$bootstrap_db_STAR_$fn__14156 = new db$bootstrap_db_STAR_$fn__14156(db6);
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object12 = midx;
        Object object13 = iLookupThunk3.get(object12);
        if (iLookupThunk3 == object13) {
            __thunk__2__ = __site__2__.fault(object12);
            object13 = __thunk__2__.get(object12);
        }
        IFn iFn3 = (IFn)const__12.getRawRoot();
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object14 = midx;
        Object object15 = iLookupThunk4.get(object14);
        if (iLookupThunk4 == object15) {
            __thunk__3__ = __site__3__.fault(object14);
            object15 = __thunk__3__.get(object14);
        }
        Object avet2 = iFn2.invoke((Object)db$bootstrap_db_STAR_$fn__14156, object13, iFn3.invoke(object15));
        IFn iFn4 = (IFn)const__6.getRawRoot();
        db$bootstrap_db_STAR_$fn__14158 db$bootstrap_db_STAR_$fn__14158 = new db$bootstrap_db_STAR_$fn__14158(db6);
        ILookupThunk iLookupThunk5 = __thunk__4__;
        Object object16 = midx;
        Object object17 = iLookupThunk5.get(object16);
        if (iLookupThunk5 == object17) {
            __thunk__4__ = __site__4__.fault(object16);
            object17 = __thunk__4__.get(object16);
        }
        IFn iFn5 = (IFn)const__12.getRawRoot();
        ILookupThunk iLookupThunk6 = __thunk__5__;
        Object object18 = midx;
        Object object19 = iLookupThunk6.get(object18);
        if (iLookupThunk6 == object19) {
            __thunk__5__ = __site__5__.fault(object18);
            object19 = __thunk__5__.get(object18);
        }
        Object raet2 = iFn4.invoke((Object)db$bootstrap_db_STAR_$fn__14158, object17, iFn5.invoke(object19));
        Object object20 = db6;
        db6 = null;
        Object object21 = midx;
        midx = null;
        Object object22 = avet2;
        avet2 = null;
        Object object23 = raet2;
        raet2 = null;
        return ((IFn)const__15.getRawRoot()).invoke(object20, (Object)const__9, ((IFn)const__15.getRawRoot()).invoke(object21, (Object)const__11, object22, (Object)const__14, object23), (Object)const__16, const__17);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$bootstrap_db_STAR_.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)"bootstrap");
    }

    public Object invoke() {
        return db$bootstrap_db_STAR_.invokeStatic();
    }
}

