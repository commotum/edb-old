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
import datomic.kv_cluster.KVCluster$doit__10886$fn__10890;
import java.nio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KVCluster$doit__10886
extends AFunction {
    Object buf;
    Object kvs;
    Object backoff;
    Object retrying_write;
    Object val_key;
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"StoragePutBytes");
    public static final Keyword const__2 = RT.keyword(null, (String)"event");
    public static final Keyword const__3 = RT.keyword((String)"kv-cluster", (String)"create-val");
    public static final Keyword const__4 = RT.keyword(null, (String)"val-key");
    public static final Keyword const__5 = RT.keyword(null, (String)"bufsize");
    public static final Var const__7 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Keyword const__8 = RT.keyword(null, (String)"StoragePutMsec");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__11 = RT.keyword(null, (String)"msec");
    public static final Keyword const__12 = RT.keyword(null, (String)"phase");
    public static final Keyword const__13 = RT.keyword(null, (String)"end");
    public static final Keyword const__14 = RT.keyword(null, (String)"threw");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__16 = RT.var((String)"datomic.slf4j", (String)"process");
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

    public KVCluster$doit__10886(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.buf = object;
        this.kvs = object2;
        this.backoff = object3;
        this.retrying_write = object4;
        this.val_key = object5;
    }

    public Object invoke() {
        Object object;
        IPersistentMap iPersistentMap;
        ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)((Buffer)this.buf).remaining());
        IPersistentMap m_10887 = RT.mapUniqueKeys((Object[])new Object[]{const__2, const__3, const__4, this.val_key, const__5, ((Buffer)this.buf).remaining()});
        long start__8981__auto__10896 = System.nanoTime();
        Object result__8982__auto__10897 = ((IFn)new KVCluster$doit__10886$fn__10890(this.buf, this.kvs, this.backoff, this.retrying_write, this.val_key)).invoke();
        long elapsed_10888 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__10896);
        Object msec_10889 = ((IFn)const__7.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_10888));
        ((IFn)const__0.getRawRoot()).invoke((Object)const__8, msec_10889);
        IFn iFn = (IFn)const__9.getRawRoot();
        IPersistentMap iPersistentMap2 = m_10887;
        m_10887 = null;
        Object object2 = msec_10889;
        msec_10889 = null;
        Object object3 = ((IFn)const__10.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__11, object2, (Object)const__12, (Object)const__13);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = result__8982__auto__10897;
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
            Object object6 = result__8982__auto__10897;
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
        Object endmsg__8984__auto__10895 = iFn.invoke(object3, iPersistentMap);
        Logger logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            Object object8 = endmsg__8984__auto__10895;
            endmsg__8984__auto__10895 = null;
            logger2.info((String)((IFn)const__16.getRawRoot()).invoke(object8));
        }
        Object object9 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__10897, (Object)const__18);
        if (object9 != null && object9 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object10 = result__8982__auto__10897;
            result__8982__auto__10897 = null;
            object = iLookupThunk3.get(object10);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object10);
                object = __thunk__2__.get(object10);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object11 = result__8982__auto__10897;
            result__8982__auto__10897 = null;
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

