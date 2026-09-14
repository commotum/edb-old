/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.connector;

import clojure.lang.AFn;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.common.AsyncShutdown;
import datomic.connector.TransactorConnector;
import datomic.connector.TransactorHornetConnector$fn__21227;
import datomic.connector.TransactorHornetConnector$fn__21230;
import datomic.connector.TransactorHornetConnector$fn__21234;
import datomic.connector.TransactorHornetConnector$fn__21238;
import datomic.connector.TransactorHornetConnector$fn__21242;
import datomic.connector.TransactorHornetConnector$fn__21244;
import datomic.connector.TransactorHornetConnector$fn__21246;
import datomic.connector.TransactorHornetConnector$fn__21249;
import datomic.connector.TransactorHornetConnector$fn__21266;
import datomic.connector.TransactorHornetConnector$reify__21277;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactorHornetConnector
implements TransactorConnector,
AsyncShutdown,
IType {
    public final Object cluster_conf;
    public final Object transactor_endpoint;
    public final Object hornet_factory;
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"clear");
    public static final Var const__2 = RT.var((String)"datomic.connector", (String)"sfb-cache");
    public static final Keyword const__3 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__4 = RT.keyword((String)"peer", (String)"request-timed-out");
    public static final Keyword const__5 = RT.keyword(null, (String)"message");
    public static final Keyword const__6 = RT.keyword(null, (String)"request");
    public static final Keyword const__7 = RT.keyword(null, (String)"result");
    public static final Keyword const__9 = RT.keyword((String)"peer", (String)"request-failed");
    public static final Keyword const__10 = RT.keyword(null, (String)"default");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__12 = RT.keyword(null, (String)"event");
    public static final Keyword const__13 = RT.keyword((String)"peer", (String)"admin-request");
    public static final Keyword const__14 = RT.keyword(null, (String)"return");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__19 = RT.keyword(null, (String)"db-id");
    public static final Var const__20 = RT.var((String)"datomic.artemis-client", (String)"start-session");
    public static final Keyword const__21 = RT.keyword(null, (String)"on-failure");
    public static final Keyword const__22 = RT.keyword(null, (String)"pre-acknowledge");
    public static final Var const__23 = RT.var((String)"datomic.error", (String)"runonce");
    public static final Var const__24 = RT.var((String)"datomic.artemis-client", (String)"create-temporary-queue");
    public static final Var const__25 = RT.var((String)"datomic.transaction", (String)"push-address");
    public static final Var const__26 = RT.var((String)"datomic.artemis-client", (String)"create-consumer");
    public static final Var const__27 = RT.var((String)"datomic.connector", (String)"create-hornet-notifier");
    public static final Var const__28 = RT.var((String)"datomic.artemis-client", (String)"create-producer");
    public static final Var const__29 = RT.var((String)"datomic.transaction", (String)"submit-address");
    public static final Var const__30 = RT.var((String)"datomic.transaction", (String)"write-handlers");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"future-call");
    public static final AFn const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 288, RT.keyword(null, (String)"column"), 6});
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"failed"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"failed"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public TransactorHornetConnector(Object object, Object object2, Object object3) {
        this.cluster_conf = object;
        this.transactor_endpoint = object2;
        this.hornet_factory = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf"), (Object)Symbol.intern(null, (String)"transactor-endpoint"), (Object)Symbol.intern(null, (String)"hornet-factory"));
    }

    public Object async_shutdown() {
        return null;
    }

    public Object endpoint() {
        return this.transactor_endpoint;
    }

    public Object start_updater(Object unsent_updates_queue, Object push_handler, Object failure_handler) {
        Delay cleanup2;
        Object fut;
        Object handlers;
        Object map__21248;
        Object object;
        Object map__212482 = this.cluster_conf;
        Object object2 = ((IFn)const__16.getRawRoot()).invoke(map__212482);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__212482;
            map__212482 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__17.getRawRoot()).invoke(object3)));
        } else {
            object = map__212482;
            map__212482 = null;
        }
        Object object4 = map__21248 = object;
        map__21248 = null;
        Object db_id = RT.get((Object)object4, (Object)const__19);
        Object session = ((IFn)const__20.getRawRoot()).invoke(this.hornet_factory, this.transactor_endpoint, (Object)const__21, failure_handler, (Object)const__22, (Object)Boolean.TRUE);
        Object object5 = db_id;
        db_id = null;
        Object hornet_producer = ((IFn)const__28.getRawRoot()).invoke(session, ((IFn)const__29.getRawRoot()).invoke(object5));
        Object object6 = handlers = ((IFn)const__30.getRawRoot()).invoke((Object)Boolean.TRUE);
        handlers = null;
        Object object7 = failure_handler;
        failure_handler = null;
        Object object8 = unsent_updates_queue;
        unsent_updates_queue = null;
        Object object9 = push_handler;
        push_handler = null;
        Object object10 = fut = ((IFn)const__31.getRawRoot()).invoke((Object)new TransactorHornetConnector$fn__21249(object6, hornet_producer, object7, session, object8, object9));
        fut = null;
        Object object11 = hornet_producer;
        hornet_producer = null;
        Object object12 = session;
        session = null;
        Delay delay = cleanup2 = new Delay((IFn)new TransactorHornetConnector$fn__21266(object10, object11, object12));
        cleanup2 = null;
        return ((IObj)new TransactorHornetConnector$reify__21277(null, delay)).withMeta((IPersistentMap)const__36);
    }

    public Object create_notifier(Object push_handler, Object failure_handler) {
        Object object;
        Object map__21229;
        Object object2;
        Object map__212292 = this.cluster_conf;
        Object object3 = ((IFn)const__16.getRawRoot()).invoke(map__212292);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__212292;
            map__212292 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__17.getRawRoot()).invoke(object4)));
        } else {
            object2 = map__212292;
            map__212292 = null;
        }
        Object object5 = map__21229 = object2;
        map__21229 = null;
        Object db_id = RT.get((Object)object5, (Object)const__19);
        Object session = ((IFn)const__20.getRawRoot()).invoke(this.hornet_factory, this.transactor_endpoint, (Object)const__21, failure_handler, (Object)const__22, (Object)Boolean.TRUE);
        Object unused = ((IFn)const__23.getRawRoot()).invoke((Object)new TransactorHornetConnector$fn__21230(session));
        try {
            Object object6;
            Object object7 = db_id;
            db_id = null;
            Object result_queue = ((IFn)const__24.getRawRoot()).invoke(session, ((IFn)const__25.getRawRoot()).invoke(object7));
            Object object8 = unused;
            unused = null;
            Object unused2 = ((IFn)const__23.getRawRoot()).invoke((Object)new TransactorHornetConnector$fn__21234(object8, session, result_queue));
            try {
                Object object9;
                Object hornet_consumer = ((IFn)const__26.getRawRoot()).invoke(session, result_queue);
                Object object10 = unused2;
                unused2 = null;
                ((IFn)const__23.getRawRoot()).invoke((Object)new TransactorHornetConnector$fn__21238(hornet_consumer, object10));
                try {
                    Object object11 = push_handler;
                    push_handler = null;
                    Object object12 = failure_handler;
                    failure_handler = null;
                    object9 = ((IFn)const__27.getRawRoot()).invoke(object11, session, result_queue, hornet_consumer, object12);
                }
                catch (Throwable t__709__auto__2) {
                    Object object13 = hornet_consumer;
                    hornet_consumer = null;
                    ((IFn)new TransactorHornetConnector$fn__21242(object13)).invoke();
                    Object t__709__auto__2 = null;
                    throw t__709__auto__2;
                }
                object6 = object9;
            }
            catch (Throwable t__709__auto__3) {
                Object object14 = result_queue;
                result_queue = null;
                ((IFn)new TransactorHornetConnector$fn__21244(session, object14)).invoke();
                Object t__709__auto__3 = null;
                throw t__709__auto__3;
            }
            object = object6;
        }
        catch (Throwable t__709__auto__4) {
            Object object15 = session;
            session = null;
            ((IFn)new TransactorHornetConnector$fn__21246(object15)).invoke();
            Object t__709__auto__4 = null;
            throw t__709__auto__4;
        }
        return object;
    }

    public Object admin_request_STAR_(Object request, Object arg2, Object timeout_msec) {
        Object object;
        Object timeout = new Object();
        Object object2 = arg2;
        arg2 = null;
        Object object3 = timeout_msec;
        timeout_msec = null;
        Object result2 = ((IFn)new TransactorHornetConnector$fn__21227(object2, object3, this.transactor_endpoint, this.hornet_factory, request, timeout)).invoke();
        Object object4 = timeout;
        timeout = null;
        if (Util.equiv((Object)object4, (Object)result2)) {
            ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot());
            Object[] objectArray = new Object[8];
            objectArray[0] = const__3;
            objectArray[1] = const__4;
            objectArray[2] = const__5;
            objectArray[3] = "Transactor request timed out";
            objectArray[4] = const__6;
            Object object5 = request;
            request = null;
            objectArray[5] = object5;
            objectArray[6] = const__7;
            Object object6 = result2;
            result2 = null;
            objectArray[7] = object6;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object7 = result2;
            Object object8 = iLookupThunk.get(object7);
            if (iLookupThunk == object8) {
                __thunk__0__ = __site__0__.fault(object7);
                object8 = __thunk__0__.get(object7);
            }
            if (object8 != null && object8 != Boolean.FALSE) {
                Object[] objectArray = new Object[8];
                objectArray[0] = const__3;
                objectArray[1] = const__9;
                objectArray[2] = const__5;
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object9 = result2;
                Object object10 = iLookupThunk2.get(object9);
                if (iLookupThunk2 == object10) {
                    __thunk__1__ = __site__1__.fault(object9);
                    object10 = __thunk__1__.get(object9);
                }
                objectArray[3] = object10;
                objectArray[4] = const__6;
                Object object11 = request;
                request = null;
                objectArray[5] = object11;
                objectArray[6] = const__7;
                Object object12 = result2;
                result2 = null;
                objectArray[7] = object12;
                object = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                Keyword keyword = const__10;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Logger logger = LoggerFactory.getLogger((String)"datomic.connector");
                    if (logger.isDebugEnabled()) {
                        Logger logger2 = logger;
                        logger = null;
                        IFn iFn = (IFn)const__11.getRawRoot();
                        Object[] objectArray = new Object[6];
                        objectArray[0] = const__12;
                        objectArray[1] = const__13;
                        objectArray[2] = const__6;
                        Object object13 = request;
                        request = null;
                        objectArray[3] = object13;
                        objectArray[4] = const__14;
                        ILookupThunk iLookupThunk3 = __thunk__2__;
                        Object object14 = result2;
                        Object object15 = iLookupThunk3.get(object14);
                        if (iLookupThunk3 == object15) {
                            __thunk__2__ = __site__2__.fault(object14);
                            object15 = __thunk__2__.get(object14);
                        }
                        objectArray[5] = object15;
                        logger2.debug((String)iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                    }
                    object = result2;
                    result2 = null;
                } else {
                    object = null;
                }
            }
        }
        return object;
    }
}

