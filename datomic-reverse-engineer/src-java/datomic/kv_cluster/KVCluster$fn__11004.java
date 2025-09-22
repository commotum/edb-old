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
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008;
import java.nio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KVCluster$fn__11004
extends AFunction {
    Object pod_garbage_handler;
    Object kvs;
    Object retrying_read;
    Object cs;
    Object retrying_write;
    Object pod_key;
    Object metamap;
    Object etag;
    Object rev;
    Object tailid;
    Object buf;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"kv-cluster", (String)"update-pod");
    public static final Keyword const__2 = RT.keyword(null, (String)"pod-key");
    public static final Keyword const__3 = RT.keyword(null, (String)"tailid");
    public static final Keyword const__4 = RT.keyword(null, (String)"rev");
    public static final Keyword const__5 = RT.keyword(null, (String)"etag");
    public static final Keyword const__6 = RT.keyword(null, (String)"bufsize");
    public static final Var const__7 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__9 = RT.keyword(null, (String)"phase");
    public static final Keyword const__10 = RT.keyword(null, (String)"begin");
    public static final Var const__12 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__13 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__14 = RT.keyword(null, (String)"PodUpdateMsec");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__16 = RT.keyword(null, (String)"msec");
    public static final Keyword const__17 = RT.keyword(null, (String)"end");
    public static final Keyword const__18 = RT.keyword(null, (String)"threw");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__21 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public KVCluster$fn__11004(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
        this.pod_garbage_handler = object;
        this.kvs = object2;
        this.retrying_read = object3;
        this.cs = object4;
        this.retrying_write = object5;
        this.pod_key = object6;
        this.metamap = object7;
        this.etag = object8;
        this.rev = object9;
        this.tailid = object10;
        this.buf = object11;
    }

    public Object invoke() {
        Object object;
        IPersistentMap iPersistentMap;
        Object[] objectArray = new Object[12];
        objectArray[0] = const__0;
        objectArray[1] = const__1;
        objectArray[2] = const__2;
        objectArray[3] = this.pod_key;
        objectArray[4] = const__3;
        objectArray[5] = this.tailid;
        objectArray[6] = const__4;
        objectArray[7] = this.rev;
        objectArray[8] = const__5;
        objectArray[9] = this.etag;
        objectArray[10] = const__6;
        Object object2 = this.buf;
        objectArray[11] = object2 != null && object2 != Boolean.FALSE ? Integer.valueOf(((Buffer)this.buf).remaining()) : null;
        IPersistentMap m_11005 = RT.mapUniqueKeys((Object[])objectArray);
        Logger logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)m_11005, (Object)const__9, (Object)const__10)));
        }
        long start__8981__auto__11035 = System.nanoTime();
        this.pod_key = null;
        this.metamap = null;
        this.etag = null;
        this.rev = null;
        this.tailid = null;
        this.buf = null;
        Object result__8982__auto__11036 = ((IFn)new KVCluster$fn__11004$fn__11008(this.pod_garbage_handler, this.kvs, this.retrying_read, this.cs, this.retrying_write, this.pod_key, this.metamap, this.etag, this.rev, this.tailid, this.buf)).invoke();
        long elapsed_11006 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__11035);
        Object msec_11007 = ((IFn)const__12.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_11006));
        ((IFn)const__13.getRawRoot()).invoke((Object)const__14, msec_11007);
        IFn iFn = (IFn)const__15.getRawRoot();
        IPersistentMap iPersistentMap2 = m_11005;
        m_11005 = null;
        Object object3 = msec_11007;
        msec_11007 = null;
        Object object4 = ((IFn)const__8.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__16, object3, (Object)const__9, (Object)const__17);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = result__8982__auto__11036;
        Object object6 = iLookupThunk.get(object5);
        if (iLookupThunk == object6) {
            __thunk__0__ = __site__0__.fault(object5);
            object6 = __thunk__0__.get(object5);
        }
        if (object6 != null && object6 != Boolean.FALSE) {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__18;
            IFn iFn2 = (IFn)const__19.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object7 = result__8982__auto__11036;
            Object object8 = iLookupThunk2.get(object7);
            if (iLookupThunk2 == object8) {
                __thunk__1__ = __site__1__.fault(object7);
                object8 = __thunk__1__.get(object7);
            }
            objectArray2[1] = iFn2.invoke(object8);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__11033 = iFn.invoke(object4, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.kv-cluster");
        if (logger3.isInfoEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object9 = endmsg__8984__auto__11033;
            endmsg__8984__auto__11033 = null;
            logger4.info((String)((IFn)const__7.getRawRoot()).invoke(object9));
        }
        Object object10 = ((IFn)const__20.getRawRoot()).invoke(result__8982__auto__11036, (Object)const__21);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object11 = result__8982__auto__11036;
            result__8982__auto__11036 = null;
            object = iLookupThunk3.get(object11);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object11);
                object = __thunk__2__.get(object11);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object12 = result__8982__auto__11036;
            result__8982__auto__11036 = null;
            Object object13 = iLookupThunk4.get(object12);
            if (iLookupThunk4 == object13) {
                __thunk__3__ = __site__3__.fault(object12);
                object13 = __thunk__3__.get(object12);
            }
            throw (Throwable)object13;
        }
        return object;
    }
}

