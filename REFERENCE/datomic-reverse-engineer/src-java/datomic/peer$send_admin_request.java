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
import datomic.peer$send_admin_request$fn__21699;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class peer$send_admin_request
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"peer", (String)"transactor-admin-request");
    public static final Keyword const__2 = RT.keyword(null, (String)"cluster");
    public static final Var const__3 = RT.var((String)"datomic.uri", (String)"loggable-cluster-conf");
    public static final Keyword const__4 = RT.keyword(null, (String)"request");
    public static final Keyword const__5 = RT.keyword(null, (String)"arg");
    public static final Var const__6 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"phase");
    public static final Keyword const__9 = RT.keyword(null, (String)"begin");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
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

    public static Object invokeStatic(Object cluster_conf, Object request, Object arg2) {
        Object object;
        IPersistentMap iPersistentMap;
        IPersistentMap m_21696 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, ((IFn)const__3.getRawRoot()).invoke(cluster_conf), const__4, request, const__5, arg2});
        Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_21696, (Object)const__8, (Object)const__9)));
        }
        long start__8981__auto__21705 = System.nanoTime();
        Object object2 = cluster_conf;
        cluster_conf = null;
        Object object3 = arg2;
        arg2 = null;
        Object object4 = request;
        request = null;
        Object result__8982__auto__21706 = ((IFn)new peer$send_admin_request$fn__21699(object2, object3, object4)).invoke();
        long elapsed_21697 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__21705);
        Object msec_21698 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_21697));
        IFn iFn = (IFn)const__12.getRawRoot();
        IPersistentMap iPersistentMap2 = m_21696;
        m_21696 = null;
        Object object5 = msec_21698;
        msec_21698 = null;
        Object object6 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__13, object5, (Object)const__8, (Object)const__14);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = result__8982__auto__21706;
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
            Object object9 = result__8982__auto__21706;
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
        Object endmsg__8984__auto__21703 = iFn.invoke(object6, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.peer");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object11 = endmsg__8984__auto__21703;
            endmsg__8984__auto__21703 = null;
            logger4.debug((String)((IFn)const__6.getRawRoot()).invoke(object11));
        }
        Object object12 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__21706, (Object)const__18);
        if (object12 != null && object12 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object13 = result__8982__auto__21706;
            result__8982__auto__21706 = null;
            object = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object13);
                object = __thunk__2__.get(object13);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object14 = result__8982__auto__21706;
            result__8982__auto__21706 = null;
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
        return peer$send_admin_request.invokeStatic(object4, object5, object6);
    }
}

