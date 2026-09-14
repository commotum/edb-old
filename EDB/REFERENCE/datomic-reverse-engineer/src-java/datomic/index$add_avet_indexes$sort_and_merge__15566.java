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
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$add_avet_indexes$sort_and_merge__15566$fn__15570;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$add_avet_indexes$sort_and_merge__15566
extends AFunction {
    Object root_map;
    Object olookup;
    Object as_of_t;
    Object cstore;
    Object attrids;
    Object db;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"index", (String)"add-avet");
    public static final Keyword const__2 = RT.keyword(null, (String)"next-t");
    public static final Keyword const__3 = RT.keyword(null, (String)"attributes");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"phase");
    public static final Keyword const__7 = RT.keyword(null, (String)"begin");
    public static final Var const__9 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__10 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__11 = RT.keyword(null, (String)"AddIndexMsec");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__13 = RT.keyword(null, (String)"msec");
    public static final Keyword const__14 = RT.keyword(null, (String)"end");
    public static final Keyword const__15 = RT.keyword(null, (String)"threw");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__18 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public index$add_avet_indexes$sort_and_merge__15566(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.root_map = object;
        this.olookup = object2;
        this.as_of_t = object3;
        this.cstore = object4;
        this.attrids = object5;
        this.db = object6;
    }

    public Object invoke(Object k, Object aevt_datoms, Object garbage2) {
        Object object;
        IPersistentMap iPersistentMap;
        IPersistentMap m_15567 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, this.as_of_t, const__3, this.attrids});
        Logger logger = LoggerFactory.getLogger((String)"datomic.index");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)m_15567, (Object)const__6, (Object)const__7)));
        }
        long start__8981__auto__15579 = System.nanoTime();
        Object object2 = k;
        k = null;
        Object object3 = garbage2;
        garbage2 = null;
        Object object4 = aevt_datoms;
        aevt_datoms = null;
        Object result__8982__auto__15580 = ((IFn)new index$add_avet_indexes$sort_and_merge__15566$fn__15570(this.root_map, this.olookup, this.as_of_t, this.cstore, object2, object3, this.db, object4)).invoke();
        long elapsed_15568 = System.nanoTime() - start__8981__auto__15579;
        Object msec_15569 = ((IFn)const__9.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15568));
        ((IFn)const__10.getRawRoot()).invoke((Object)const__11, msec_15569);
        IFn iFn = (IFn)const__12.getRawRoot();
        IPersistentMap iPersistentMap2 = m_15567;
        m_15567 = null;
        Object object5 = msec_15569;
        msec_15569 = null;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__13, object5, (Object)const__6, (Object)const__14);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = result__8982__auto__15580;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        if (object8 != null && object8 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__15;
            IFn iFn2 = (IFn)const__16.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object9 = result__8982__auto__15580;
            Object object10 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            objectArray[1] = iFn2.invoke(object10);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__15577 = iFn.invoke(object6, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.index");
        if (logger3.isInfoEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object11 = endmsg__8984__auto__15577;
            endmsg__8984__auto__15577 = null;
            logger4.info((String)((IFn)const__4.getRawRoot()).invoke(object11));
        }
        Object object12 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__15580, (Object)const__18);
        if (object12 != null && object12 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object13 = result__8982__auto__15580;
            result__8982__auto__15580 = null;
            object = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object13);
                object = __thunk__2__.get(object13);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object14 = result__8982__auto__15580;
            result__8982__auto__15580 = null;
            Object object15 = iLookupThunk4.get(object14);
            if (iLookupThunk4 == object15) {
                __thunk__3__ = __site__3__.fault(object14);
                object15 = __thunk__3__.get(object14);
            }
            throw (Throwable)object15;
        }
        return object;
    }
}

