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
import datomic.coordination$lookup_endpoint$fn__11694;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class coordination$lookup_endpoint
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"coord", (String)"lookup-endpoint");
    public static final Keyword const__2 = RT.keyword(null, (String)"k");
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

    public static Object invokeStatic(Object cluster2, Object k) {
        Object object;
        IPersistentMap iPersistentMap;
        IPersistentMap m_11691 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, k});
        Logger logger = LoggerFactory.getLogger((String)"datomic.coordination");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_11691, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__11700 = System.nanoTime();
        Object object2 = k;
        k = null;
        Object object3 = cluster2;
        cluster2 = null;
        Object result__8982__auto__11701 = ((IFn)new coordination$lookup_endpoint$fn__11694(object2, object3)).invoke();
        long elapsed_11692 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__11700);
        Object msec_11693 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_11692));
        IFn iFn = (IFn)const__9.getRawRoot();
        IPersistentMap iPersistentMap2 = m_11691;
        m_11691 = null;
        Object object4 = msec_11693;
        msec_11693 = null;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__10, object4, (Object)const__5, (Object)const__11);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = result__8982__auto__11701;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        if (object7 != null && object7 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__12;
            IFn iFn2 = (IFn)const__13.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object8 = result__8982__auto__11701;
            Object object9 = iLookupThunk2.get(object8);
            if (iLookupThunk2 == object9) {
                __thunk__1__ = __site__1__.fault(object8);
                object9 = __thunk__1__.get(object8);
            }
            objectArray[1] = iFn2.invoke(object9);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__11698 = iFn.invoke(object5, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.coordination");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object10 = endmsg__8984__auto__11698;
            endmsg__8984__auto__11698 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object10));
        }
        Object object11 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__11701, (Object)const__15);
        if (object11 != null && object11 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object12 = result__8982__auto__11701;
            result__8982__auto__11701 = null;
            object = iLookupThunk3.get(object12);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object12);
                object = __thunk__2__.get(object12);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object13 = result__8982__auto__11701;
            result__8982__auto__11701 = null;
            Object object14 = iLookupThunk4.get(object13);
            if (iLookupThunk4 == object14) {
                __thunk__3__ = __site__3__.fault(object13);
                object14 = __thunk__3__.get(object13);
            }
            throw (Throwable)object14;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return coordination$lookup_endpoint.invokeStatic(object3, object4);
    }
}

