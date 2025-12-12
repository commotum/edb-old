/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.kv_cluster.KVCluster$fn__10981$fn__10985$fn__10986;
import datomic.kv_cluster.KVCluster$fn__10981$fn__10985$fn__10989;
import datomic.kv_cluster.KVCluster$fn__10981$fn__10985$fn__10994;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KVCluster$fn__10981$fn__10985
extends AFunction {
    Object kvs;
    Object retrying_read;
    Object pod_key;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"linear");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"rev");
    public static final Keyword const__6 = RT.keyword(null, (String)"tail");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__12 = RT.keyword(null, (String)"event");
    public static final Keyword const__13 = RT.keyword((String)"kv-cluster", (String)"pod-size");
    public static final Keyword const__14 = RT.keyword(null, (String)"bytes");
    public static final Keyword const__15 = RT.keyword(null, (String)"chunks");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__22 = RT.keyword(null, (String)"id");
    public static final Keyword const__23 = RT.keyword(null, (String)"etag");
    public static final Keyword const__24 = RT.keyword(null, (String)"buf");
    public static final Keyword const__25 = RT.keyword(null, (String)"threw");

    public KVCluster$fn__10981$fn__10985(Object object, Object object2, Object object3) {
        this.kvs = object;
        this.retrying_read = object2;
        this.pod_key = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object temp__5457__auto__10998;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.pod_key = null;
            Object object2 = temp__5457__auto__10998 = ((IFn)this.retrying_read).invoke((Object)const__1, (Object)new KVCluster$fn__10981$fn__10985$fn__10986(this.kvs, this.pod_key));
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3;
                Object pref;
                Object object4 = temp__5457__auto__10998;
                temp__5457__auto__10998 = null;
                Object map__10988 = pref = object4;
                Object object5 = ((IFn)const__2.getRawRoot()).invoke(map__10988);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__10988;
                    map__10988 = null;
                    object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object6)));
                } else {
                    object3 = map__10988;
                    map__10988 = null;
                }
                Object map__109882 = object3;
                Object rev = RT.get((Object)map__109882, (Object)const__5);
                Object object7 = map__109882;
                map__109882 = null;
                Object tail = RT.get((Object)object7, (Object)const__6);
                Object bufs = ((IFn)new KVCluster$fn__10981$fn__10985$fn__10989(this.kvs, this.retrying_read, tail)).invoke();
                Object len = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__9.getRawRoot()).invoke((Object)new KVCluster$fn__10981$fn__10985$fn__10994(), bufs));
                byte[] arr = Numbers.byte_array((Object)len);
                Logger logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
                if (logger.isDebugEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    Object[] objectArray2 = new Object[6];
                    objectArray2[0] = const__12;
                    objectArray2[1] = const__13;
                    objectArray2[2] = const__14;
                    Object object8 = len;
                    len = null;
                    objectArray2[3] = object8;
                    objectArray2[4] = const__15;
                    objectArray2[5] = RT.count((Object)bufs);
                    logger2.debug((String)((IFn)const__11.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray2)));
                }
                long i = 0L;
                Object object9 = bufs;
                bufs = null;
                Object bs = ((IFn)const__3.getRawRoot()).invoke(object9);
                while (true) {
                    Object temp__5457__auto__10997;
                    Object object10 = temp__5457__auto__10997 = ((IFn)const__18.getRawRoot()).invoke(bs);
                    if (object10 == null || object10 == Boolean.FALSE) break;
                    Object object11 = temp__5457__auto__10997;
                    temp__5457__auto__10997 = null;
                    Object b = object11;
                    ((ByteBuffer)b).duplicate().get(arr, RT.intCast((long)i), ((Buffer)b).remaining());
                    Object object12 = b;
                    b = null;
                    Object object13 = bs;
                    bs = null;
                    bs = ((IFn)const__19.getRawRoot()).invoke(object13);
                    i = Numbers.add((long)i, (long)((Buffer)object12).remaining());
                }
                Object object14 = pref;
                pref = null;
                Object[] objectArray3 = new Object[6];
                objectArray3[0] = const__5;
                Object object15 = rev;
                rev = null;
                objectArray3[1] = object15;
                objectArray3[2] = const__23;
                Object object16 = tail;
                tail = null;
                objectArray3[3] = object16;
                objectArray3[4] = const__24;
                byte[] byArray = arr;
                arr = null;
                objectArray3[5] = ByteBuffer.wrap(byArray);
                object = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke(object14, (Object)const__22, (Object)const__6), (Object)RT.mapUniqueKeys((Object[])objectArray3));
            } else {
                object = null;
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__25;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

