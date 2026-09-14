/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
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

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.process_monitor$report_metrics$fn__23512;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class process_monitor$report_metrics
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.process-monitor", (String)"snapshot-metrics");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__3 = RT.keyword(null, (String)"event");
    public static final Keyword const__4 = RT.keyword(null, (String)"metrics");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"metrics", (String)"report")});
    public static final Keyword const__7 = RT.keyword(null, (String)"phase");
    public static final Keyword const__8 = RT.keyword(null, (String)"begin");
    public static final Var const__10 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__12 = RT.keyword(null, (String)"msec");
    public static final Keyword const__13 = RT.keyword(null, (String)"end");
    public static final Keyword const__14 = RT.keyword(null, (String)"threw");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__17 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object callback2) {
        Object object;
        IPersistentMap iPersistentMap;
        Object m = ((IFn)const__0.getRawRoot()).invoke();
        Logger logger = LoggerFactory.getLogger((String)"datomic.process-monitor");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(m, (Object)const__3, (Object)const__4)));
        }
        AFn m_23509 = const__6;
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.process-monitor");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            logger4.debug((String)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)m_23509, (Object)const__7, (Object)const__8)));
        }
        long start__8981__auto__23519 = System.nanoTime();
        Object object2 = callback2;
        callback2 = null;
        Object object3 = m;
        m = null;
        Object result__8982__auto__23520 = ((IFn)new process_monitor$report_metrics$fn__23512(object2, object3)).invoke();
        long elapsed_23510 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__23519);
        Object msec_23511 = ((IFn)const__10.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_23510));
        IFn iFn = (IFn)const__11.getRawRoot();
        AFn aFn = m_23509;
        m_23509 = null;
        Object object4 = msec_23511;
        msec_23511 = null;
        Object object5 = ((IFn)const__2.getRawRoot()).invoke((Object)aFn, (Object)const__12, object4, (Object)const__7, (Object)const__13);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = result__8982__auto__23520;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        if (object7 != null && object7 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__14;
            IFn iFn2 = (IFn)const__15.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object8 = result__8982__auto__23520;
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
        Object endmsg__8984__auto__23517 = iFn.invoke(object5, iPersistentMap);
        Logger logger5 = LoggerFactory.getLogger((String)"datomic.process-monitor");
        if (logger5.isDebugEnabled()) {
            Logger logger6 = logger5;
            logger5 = null;
            Object object10 = endmsg__8984__auto__23517;
            endmsg__8984__auto__23517 = null;
            logger6.debug((String)((IFn)const__1.getRawRoot()).invoke(object10));
        }
        Object object11 = ((IFn)const__16.getRawRoot()).invoke(result__8982__auto__23520, (Object)const__17);
        if (object11 != null && object11 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object12 = result__8982__auto__23520;
            result__8982__auto__23520 = null;
            object = iLookupThunk3.get(object12);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object12);
                object = __thunk__2__.get(object12);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object13 = result__8982__auto__23520;
            result__8982__auto__23520 = null;
            Object object14 = iLookupThunk4.get(object13);
            if (iLookupThunk4 == object14) {
                __thunk__3__ = __site__3__.fault(object13);
                object14 = __thunk__3__.get(object13);
            }
            throw (Throwable)object14;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return process_monitor$report_metrics.invokeStatic(object2);
    }
}

