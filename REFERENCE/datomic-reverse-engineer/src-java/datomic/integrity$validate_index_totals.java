/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
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
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$validate_index_totals$total_datoms__22494;

public final class integrity$validate_index_totals
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"datom-counts");
    public static final Var const__1 = RT.var((String)"datomic.stats", (String)"eavt");
    public static final Var const__2 = RT.var((String)"datomic.stats", (String)"aevt");
    public static final Keyword const__3 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__4 = RT.keyword(null, (String)"aevt");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"total-datoms");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"total-datoms"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"total-datoms"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2) {
        Object eavt2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), db2);
        Object object = db2;
        db2 = null;
        Object aevt2 = ((IFn)const__0.getRawRoot()).invoke(const__2.getRawRoot(), object);
        IPersistentMap summary2 = RT.mapUniqueKeys((Object[])new Object[]{const__3, eavt2, const__4, aevt2});
        integrity$validate_index_totals$total_datoms__22494 total_datoms = new integrity$validate_index_totals$total_datoms__22494();
        Object object2 = eavt2;
        Object object3 = eavt2;
        eavt2 = null;
        Object eavt3 = ((IFn)const__5.getRawRoot()).invoke(object2, (Object)const__6, ((IFn)total_datoms).invoke(object3));
        Object object4 = aevt2;
        integrity$validate_index_totals$total_datoms__22494 integrity$validate_index_totals$total_datoms__22494 = total_datoms;
        total_datoms = null;
        Object object5 = aevt2;
        aevt2 = null;
        Object aevt3 = ((IFn)const__5.getRawRoot()).invoke(object4, (Object)const__6, ((IFn)integrity$validate_index_totals$total_datoms__22494).invoke(object5));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = eavt3;
        eavt3 = null;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object8 = aevt3;
        aevt3 = null;
        Object object9 = iLookupThunk2.get(object8);
        if (iLookupThunk2 == object9) {
            __thunk__1__ = __site__1__.fault(object8);
            object9 = __thunk__1__.get(object8);
        }
        if (!Util.equiv((Object)object7, (Object)object9)) {
            Object object10 = ((IFn)const__9.getRawRoot()).invoke((Object)"total datoms not equal", (Object)summary2);
            IPersistentMap iPersistentMap = summary2;
            summary2 = null;
            throw (Throwable)((IFn)const__8.getRawRoot()).invoke(object10, (Object)iPersistentMap);
        }
        IPersistentMap iPersistentMap = summary2;
        summary2 = null;
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_index_totals.invokeStatic(object2);
    }
}

