/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.connector$try_hornet_connect$fn__21152;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class connector$try_hornet_connect
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"peer", (String)"hornet-connect");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Var const__5 = RT.var((String)"datomic.connector", (String)"sfb-cache");
    public static final Keyword const__6 = RT.keyword((String)"peer", (String)"hornet-reuse-factory");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__8 = RT.var((String)"datomic.artemis-client", (String)"create-connector");
    public static final Keyword const__9 = RT.keyword(null, (String)"verifyHost");
    public static final Keyword const__10 = RT.keyword(null, (String)"trustStorePath");
    public static final Keyword const__11 = RT.keyword(null, (String)"trustStorePassword");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__14 = RT.var((String)"datomic.artemis-client", (String)"create-session-factory");
    public static final Var const__15 = RT.var((String)"datomic.cleanup", (String)"register-cleanup");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__17 = RT.var((String)"datomic.cleanup", (String)"shared-manager-ref");
    public static final Var const__18 = RT.var((String)"datomic.cache", (String)"put");
    public static final Keyword const__19 = RT.keyword((String)"peer", (String)"hornet-connect-failed");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"host"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"host"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"host"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object conn_factory, Object conn_args, Object session_args) {
        Object object;
        Logger logger = LoggerFactory.getLogger((String)"datomic.connector");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            IFn iFn = (IFn)const__0.getRawRoot();
            Object[] objectArray = new Object[4];
            objectArray[0] = const__1;
            objectArray[1] = const__2;
            objectArray[2] = const__3;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object2 = conn_args;
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            objectArray[3] = object3;
            logger2.debug((String)iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
        }
        IPersistentVector cache_key = Tuple.create((Object)conn_factory, (Object)conn_args, (Object)session_args);
        try {
            Object object4;
            Object temp__5455__auto__21155;
            Object object5 = temp__5455__auto__21155 = RT.get((Object)const__5.getRawRoot(), (Object)cache_key);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = temp__5455__auto__21155;
                temp__5455__auto__21155 = null;
                Object bundle = object6;
                Logger logger3 = LoggerFactory.getLogger((String)"datomic.connector");
                if (logger3.isDebugEnabled()) {
                    Logger logger4 = logger3;
                    logger3 = null;
                    IFn iFn = (IFn)const__0.getRawRoot();
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__1;
                    objectArray[1] = const__6;
                    objectArray[2] = const__3;
                    ILookupThunk iLookupThunk = __thunk__1__;
                    Object object7 = conn_args;
                    Object object8 = iLookupThunk.get(object7);
                    if (iLookupThunk == object8) {
                        __thunk__1__ = __site__1__.fault(object7);
                        object8 = __thunk__1__.get(object7);
                    }
                    objectArray[3] = object8;
                    logger4.debug((String)iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                }
                object4 = bundle;
                bundle = null;
            } else {
                Object connector2;
                Object object9 = conn_factory;
                conn_factory = null;
                Object object10 = connector2 = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), object9, (Object)const__9, (Object)Boolean.FALSE, (Object)const__10, (Object)"datomic/transactor-trust.jks", (Object)const__11, (Object)"transactor", ((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), conn_args));
                connector2 = null;
                Object object11 = session_args;
                session_args = null;
                Object bundle = ((IFn)const__14.getRawRoot()).invoke(object10, object11);
                ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(const__17.getRawRoot()), bundle, (Object)new connector$try_hornet_connect$fn__21152(bundle));
                IPersistentVector iPersistentVector = cache_key;
                cache_key = null;
                ((IFn)const__18.getRawRoot()).invoke(const__5.getRawRoot(), (Object)iPersistentVector, bundle);
                object4 = bundle;
                bundle = null;
            }
            object = object4;
        }
        catch (Throwable e2) {
            Logger logger5 = LoggerFactory.getLogger((String)"datomic.connector");
            if (logger5.isDebugEnabled()) {
                Logger logger6 = logger5;
                logger5 = null;
                IFn iFn = (IFn)const__0.getRawRoot();
                Object[] objectArray = new Object[4];
                objectArray[0] = const__1;
                objectArray[1] = const__19;
                objectArray[2] = const__3;
                ILookupThunk iLookupThunk = __thunk__2__;
                Object object12 = conn_args;
                conn_args = null;
                Object object13 = iLookupThunk.get(object12);
                if (iLookupThunk == object13) {
                    __thunk__2__ = __site__2__.fault(object12);
                    object13 = __thunk__2__.get(object12);
                }
                objectArray[3] = object13;
                logger6.debug((String)iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            Object e2 = null;
            object = e2;
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
        return connector$try_hornet_connect.invokeStatic(object4, object5, object6);
    }
}

