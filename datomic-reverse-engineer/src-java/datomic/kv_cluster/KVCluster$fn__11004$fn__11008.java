/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.kv_cluster;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008$fn__11009;
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008$fn__11011;
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008$fn__11013;
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008$fn__11015;
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008$fn__11022;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KVCluster$fn__11004$fn__11008
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
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"id");
    public static final Keyword const__2 = RT.keyword(null, (String)"v");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword(null, (String)"prev");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"not");
    public static final Keyword const__8 = RT.keyword(null, (String)"tail");
    public static final Keyword const__9 = RT.keyword(null, (String)"linear");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__12 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"or"), Symbol.intern(null, (String)"buf"), Symbol.intern(null, (String)"etag")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 18}));
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__14 = RT.keyword(null, (String)"rev");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Keyword const__16 = RT.keyword(null, (String)"etag");
    public static final Keyword const__17 = RT.keyword(null, (String)"buf");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__22 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__23 = RT.keyword(null, (String)"event");
    public static final Keyword const__24 = RT.keyword((String)"kv-cluster", (String)"update-pod");
    public static final Keyword const__25 = RT.keyword(null, (String)"pod-update-resume");
    public static final Keyword const__26 = RT.keyword(null, (String)"pod-key");
    public static final AFn const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"failed"), RT.keyword(null, (String)"conflict")});
    public static final AFn const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"failed"), RT.keyword(null, (String)"conflict")});
    public static final Keyword const__31 = RT.keyword(null, (String)"threw");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tail"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public KVCluster$fn__11004$fn__11008(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
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
        IPersistentMap iPersistentMap;
        try {
            AFn aFn;
            Object object;
            Object object2;
            Object or__5238__auto__11027;
            Object object3;
            Boolean reset_QMARK_;
            Object object4;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            IPersistentMap tail = RT.mapUniqueKeys((Object[])new Object[]{const__1, this.tailid, const__2, this.buf});
            Object object5 = this.etag;
            if (object5 != null && object5 != Boolean.FALSE) {
                IPersistentMap iPersistentMap2 = tail;
                tail = null;
                object4 = ((IFn)const__3.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__4, this.etag);
            } else {
                object4 = tail;
                tail = null;
            }
            IPersistentMap tail2 = object4;
            boolean and__5236__auto__11026 = Util.identical((Object)this.etag, null);
            Boolean bl = reset_QMARK_ = and__5236__auto__11026 ? ((IFn)const__6.getRawRoot()).invoke((Object)(Numbers.isZero((Object)this.rev) ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__11026 ? Boolean.TRUE : Boolean.FALSE);
            if (bl != null && bl != Boolean.FALSE) {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object6 = ((IFn)this.retrying_read).invoke((Object)const__9, (Object)new KVCluster$fn__11004$fn__11008$fn__11009(this.kvs, this.pod_key));
                object3 = iLookupThunk.get(object6);
                if (iLookupThunk == object3) {
                    __thunk__0__ = __site__0__.fault(object6);
                    object3 = __thunk__0__.get(object6);
                }
            } else {
                object3 = null;
            }
            Object oldtail = object3;
            Object object7 = or__5238__auto__11027 = this.buf;
            if (object7 != null && object7 != Boolean.FALSE) {
                object2 = or__5238__auto__11027;
                or__5238__auto__11027 = null;
            } else {
                object2 = this.etag;
            }
            if (object2 == null || object2 == Boolean.FALSE) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke(const__12))));
            }
            boolean or__5238__auto__11028 = Util.identical((Object)this.buf, null);
            if (or__5238__auto__11028) {
                object = or__5238__auto__11028 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                IPersistentMap iPersistentMap3 = tail2;
                tail2 = null;
                object = ((IFn)this.retrying_write).invoke((Object)const__9, (Object)new KVCluster$fn__11004$fn__11008$fn__11011(this.kvs, iPersistentMap3));
            }
            if (object != null && object != Boolean.FALSE) {
                Object item;
                this.metamap = null;
                Object object8 = item = ((IFn)const__13.getRawRoot()).invoke(this.metamap, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, this.pod_key, const__14, this.rev, const__8, this.tailid}));
                item = null;
                this.etag = null;
                Object object9 = ((IFn)this.retrying_write).invoke((Object)const__9, (Object)new KVCluster$fn__11004$fn__11008$fn__11013(object8, this.kvs, this.etag, this.rev));
                if (object9 != null && object9 != Boolean.FALSE) {
                    Object object10;
                    Boolean and__5236__auto__11029;
                    Boolean bl2 = reset_QMARK_;
                    reset_QMARK_ = null;
                    Boolean bl3 = and__5236__auto__11029 = bl2;
                    if (bl3 != null && bl3 != Boolean.FALSE) {
                        object10 = oldtail;
                    } else {
                        object10 = and__5236__auto__11029;
                        and__5236__auto__11029 = null;
                    }
                    if (object10 != null && object10 != Boolean.FALSE) {
                        Object object11 = oldtail;
                        oldtail = null;
                        ((IFn)const__15.getRawRoot()).invoke((Object)new KVCluster$fn__11004$fn__11008$fn__11015(this.pod_garbage_handler, this.kvs, this.retrying_read, this.cs, object11));
                    }
                    aFn = RT.mapUniqueKeys((Object[])new Object[]{const__14, this.rev = null, const__16, this.tailid = null, const__17, this.buf = null});
                } else {
                    Object temp__5455__auto__11031;
                    Object object12 = temp__5455__auto__11031 = ((IFn)this.retrying_read).invoke((Object)const__9, (Object)new KVCluster$fn__11004$fn__11008$fn__11022(this.kvs, this.pod_key));
                    if (object12 != null && object12 != Boolean.FALSE) {
                        boolean bl4;
                        Object object13;
                        Object object14 = temp__5455__auto__11031;
                        temp__5455__auto__11031 = null;
                        Object map__11024 = object14;
                        Object object15 = ((IFn)const__18.getRawRoot()).invoke(map__11024);
                        if (object15 != null && object15 != Boolean.FALSE) {
                            Object object16 = map__11024;
                            map__11024 = null;
                            object13 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__19.getRawRoot()).invoke(object16)));
                        } else {
                            object13 = map__11024;
                            map__11024 = null;
                        }
                        Object map__110242 = object13;
                        Object nrev = RT.get((Object)map__110242, (Object)const__14);
                        Object object17 = map__110242;
                        map__110242 = null;
                        Object ntail = RT.get((Object)object17, (Object)const__8);
                        Object object18 = nrev;
                        nrev = null;
                        boolean and__5236__auto__11030 = Util.equiv((Object)this.rev, (Object)object18);
                        if (and__5236__auto__11030) {
                            Object object19 = ntail;
                            ntail = null;
                            bl4 = Util.equiv((Object)this.tailid, (Object)object19);
                        } else {
                            bl4 = and__5236__auto__11030;
                        }
                        if (bl4) {
                            Logger logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
                            if (logger.isInfoEnabled()) {
                                Logger logger2 = logger;
                                logger = null;
                                logger2.info((String)((IFn)const__22.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__23, const__24, const__25, Boolean.TRUE, const__26, this.pod_key = null, const__14, this.rev})));
                            }
                            aFn = RT.mapUniqueKeys((Object[])new Object[]{const__14, this.rev = null, const__16, this.tailid = null, const__17, this.buf = null});
                        } else {
                            aFn = const__29;
                        }
                    } else {
                        aFn = const__30;
                    }
                }
            } else {
                throw (Throwable)new Error("kv write failed in update-pod");
            }
            objectArray[1] = aFn;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__31;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

