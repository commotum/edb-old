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
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.kv_cluster.KVCluster$fn__10898$fn__10902;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KVCluster$fn__10898
extends AFunction {
    Object protocol_nsec_k;
    Object kvs;
    Object val_key;
    Object retrying_read;
    long start;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"kv-cluster", (String)"get-val");
    public static final Keyword const__2 = RT.keyword(null, (String)"val-key");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"phase");
    public static final Keyword const__6 = RT.keyword(null, (String)"begin");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__9 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__10 = RT.keyword(null, (String)"StorageGetMsec");
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

    public KVCluster$fn__10898(Object object, Object object2, Object object3, Object object4, long l) {
        this.protocol_nsec_k = object;
        this.kvs = object2;
        this.val_key = object3;
        this.retrying_read = object4;
        this.start = l;
    }

    public Object invoke() {
        Object object;
        IPersistentMap iPersistentMap;
        IPersistentMap m_10899 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, this.val_key});
        Logger logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_10899, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__10911 = System.nanoTime();
        this.val_key = null;
        Object result__8982__auto__10912 = ((IFn)new KVCluster$fn__10898$fn__10902(this.protocol_nsec_k, this.kvs, this.val_key, this.retrying_read, this.start)).invoke();
        long elapsed_10900 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__10911);
        Object msec_10901 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_10900));
        ((IFn)const__9.getRawRoot()).invoke((Object)const__10, msec_10901);
        IFn iFn = (IFn)const__11.getRawRoot();
        IPersistentMap iPersistentMap2 = m_10899;
        m_10899 = null;
        Object object2 = msec_10901;
        msec_10901 = null;
        Object object3 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__12, object2, (Object)const__5, (Object)const__13);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = result__8982__auto__10912;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        if (object5 != null && object5 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__14;
            IFn iFn2 = (IFn)const__15.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object6 = result__8982__auto__10912;
            Object object7 = iLookupThunk2.get(object6);
            if (iLookupThunk2 == object7) {
                __thunk__1__ = __site__1__.fault(object6);
                object7 = __thunk__1__.get(object6);
            }
            objectArray[1] = iFn2.invoke(object7);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__10909 = iFn.invoke(object3, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.kv-cluster");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object8 = endmsg__8984__auto__10909;
            endmsg__8984__auto__10909 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object8));
        }
        Object object9 = ((IFn)const__16.getRawRoot()).invoke(result__8982__auto__10912, (Object)const__17);
        if (object9 != null && object9 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object10 = result__8982__auto__10912;
            result__8982__auto__10912 = null;
            object = iLookupThunk3.get(object10);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object10);
                object = __thunk__2__.get(object10);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object11 = result__8982__auto__10912;
            result__8982__auto__10912 = null;
            Object object12 = iLookupThunk4.get(object11);
            if (iLookupThunk4 == object12) {
                __thunk__3__ = __site__3__.fault(object11);
                object12 = __thunk__3__.get(object11);
            }
            throw (Throwable)object12;
        }
        return object;
    }
}

