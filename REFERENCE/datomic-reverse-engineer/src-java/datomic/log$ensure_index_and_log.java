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
import datomic.log$ensure_index_and_log$fn__16381;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class log$ensure_index_and_log
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"transactor", (String)"ensure-index-and-log");
    public static final Keyword const__2 = RT.keyword(null, (String)"db-id");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"phase");
    public static final Keyword const__6 = RT.keyword(null, (String)"begin");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__10 = RT.keyword(null, (String)"msec");
    public static final Keyword const__11 = RT.keyword(null, (String)"end");
    public static final Keyword const__12 = RT.keyword(null, (String)"threw");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__15 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object cluster2, Object olookup, Object db_id) {
        Object object;
        IPersistentMap iPersistentMap;
        IPersistentMap m_16378 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, db_id});
        Logger logger = LoggerFactory.getLogger((String)"datomic.log");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_16378, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__16389 = System.nanoTime();
        Object object2 = db_id;
        db_id = null;
        Object object3 = olookup;
        olookup = null;
        Object object4 = cluster2;
        cluster2 = null;
        Object result__8982__auto__16390 = ((IFn)new log$ensure_index_and_log$fn__16381(object2, object3, object4)).invoke();
        long elapsed_16379 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__16389);
        Object msec_16380 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_16379));
        IFn iFn = (IFn)const__9.getRawRoot();
        IPersistentMap iPersistentMap2 = m_16378;
        m_16378 = null;
        Object object5 = msec_16380;
        msec_16380 = null;
        Object object6 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__10, object5, (Object)const__5, (Object)const__11);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = result__8982__auto__16390;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        if (object8 != null && object8 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__12;
            IFn iFn2 = (IFn)const__13.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object9 = result__8982__auto__16390;
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
        Object endmsg__8984__auto__16387 = iFn.invoke(object6, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.log");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object11 = endmsg__8984__auto__16387;
            endmsg__8984__auto__16387 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object11));
        }
        Object object12 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__16390, (Object)const__15);
        if (object12 != null && object12 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object13 = result__8982__auto__16390;
            result__8982__auto__16390 = null;
            object = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object13);
                object = __thunk__2__.get(object13);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object14 = result__8982__auto__16390;
            result__8982__auto__16390 = null;
            Object object15 = iLookupThunk4.get(object14);
            if (iLookupThunk4 == object15) {
                __thunk__3__ = __site__3__.fault(object14);
                object15 = __thunk__3__.get(object14);
            }
            throw (Throwable)object15;
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
        return log$ensure_index_and_log.invokeStatic(object4, object5, object6);
    }
}

