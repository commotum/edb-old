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
package datomic.connector;

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
import datomic.connector.TransactorHornetConnector$fn__21249$fn__21253;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactorHornetConnector$fn__21249
extends AFunction {
    Object handlers;
    Object hornet_producer;
    Object failure_handler;
    Object session;
    Object unsent_updates_queue;
    Object push_handler;
    public static final AFn const__2 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"connector", (String)"updater-loop")});
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
    public static final Var const__16 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__17 = RT.var((String)"datomic.monitor", (String)"alarm");
    public static final Keyword const__18 = RT.keyword(null, (String)"UnhandledException");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public TransactorHornetConnector$fn__21249(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.handlers = object;
        this.hornet_producer = object2;
        this.failure_handler = object3;
        this.session = object4;
        this.unsent_updates_queue = object5;
        this.push_handler = object6;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            IPersistentMap iPersistentMap;
            AFn m_21250 = const__2;
            Logger logger = LoggerFactory.getLogger((String)"datomic.connector");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_21250, (Object)const__5, (Object)const__6)));
            }
            long start__8981__auto__21264 = System.nanoTime();
            this.handlers = null;
            this.hornet_producer = null;
            this.failure_handler = null;
            this.session = null;
            this.unsent_updates_queue = null;
            this.push_handler = null;
            Object result__8982__auto__21265 = ((IFn)new TransactorHornetConnector$fn__21249$fn__21253(this.handlers, this.hornet_producer, this.failure_handler, this.session, this.unsent_updates_queue, this.push_handler)).invoke();
            long elapsed_21251 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__21264);
            Object msec_21252 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_21251));
            IFn iFn = (IFn)const__9.getRawRoot();
            AFn aFn = m_21250;
            m_21250 = null;
            Object object3 = msec_21252;
            msec_21252 = null;
            Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)aFn, (Object)const__10, object3, (Object)const__5, (Object)const__11);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = result__8982__auto__21265;
            Object object6 = iLookupThunk.get(object5);
            if (iLookupThunk == object6) {
                __thunk__0__ = __site__0__.fault(object5);
                object6 = __thunk__0__.get(object5);
            }
            if (object6 != null && object6 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__12;
                IFn iFn2 = (IFn)const__13.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object7 = result__8982__auto__21265;
                Object object8 = iLookupThunk2.get(object7);
                if (iLookupThunk2 == object8) {
                    __thunk__1__ = __site__1__.fault(object7);
                    object8 = __thunk__1__.get(object7);
                }
                objectArray[1] = iFn2.invoke(object8);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__21262 = iFn.invoke(object4, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.connector");
            if (logger3.isDebugEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object9 = endmsg__8984__auto__21262;
                endmsg__8984__auto__21262 = null;
                logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object9));
            }
            Object object10 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__21265, (Object)const__15);
            if (object10 != null && object10 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object11 = result__8982__auto__21265;
                result__8982__auto__21265 = null;
                object2 = iLookupThunk3.get(object11);
                if (iLookupThunk3 == object2) {
                    __thunk__2__ = __site__2__.fault(object11);
                    object2 = __thunk__2__.get(object11);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object12 = result__8982__auto__21265;
                result__8982__auto__21265 = null;
                Object object13 = iLookupThunk4.get(object12);
                if (iLookupThunk4 == object13) {
                    __thunk__3__ = __site__3__.fault(object12);
                    object13 = __thunk__3__.get(object12);
                }
                throw (Throwable)object13;
            }
            object = object2;
        }
        catch (Throwable t__9147__auto__2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.connector");
            Throwable ex = t__9147__auto__2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__3.getRawRoot()).invoke((Object)"error executing future"), ex);
                Logger logger5 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__16.getRawRoot()).invoke((Object)logger5, (Object)throwable);
            }
            ((IFn)const__17.getRawRoot()).invoke((Object)const__18);
            Object t__9147__auto__2 = null;
            throw t__9147__auto__2;
        }
        return object;
    }
}

