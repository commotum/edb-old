/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_cluster.KVCluster$fn__10953$fn__10957$fn__10958;
import datomic.kv_cluster.KVCluster$fn__10953$fn__10957$fn__10960;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KVCluster$fn__10953$fn__10957
extends AFunction {
    Object vkey;
    Object kvs;
    Object ref_key;
    Object retrying_read;
    Object retrying_write;
    Object rev;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"id");
    public static final Keyword const__2 = RT.keyword(null, (String)"rev");
    public static final Keyword const__3 = RT.keyword(null, (String)"key");
    public static final Keyword const__5 = RT.keyword(null, (String)"ok");
    public static final Keyword const__6 = RT.keyword(null, (String)"linear");
    public static final Var const__7 = RT.var((String)"datomic.kv-cluster", (String)"same-ref?");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__9 = RT.keyword(null, (String)"event");
    public static final Keyword const__10 = RT.keyword((String)"kv-cluster", (String)"set-ref-resume");
    public static final Keyword const__11 = RT.keyword(null, (String)"ref-key");
    public static final Keyword const__12 = RT.keyword(null, (String)"conflict");
    public static final Keyword const__13 = RT.keyword(null, (String)"threw");

    public KVCluster$fn__10953$fn__10957(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.vkey = object;
        this.kvs = object2;
        this.ref_key = object3;
        this.retrying_read = object4;
        this.retrying_write = object5;
        this.rev = object6;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Keyword keyword;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.vkey = null;
            IPersistentMap item = RT.mapUniqueKeys((Object[])new Object[]{const__1, this.ref_key, const__2, this.rev, const__3, this.vkey});
            if (Util.equiv((Object)const__5, (Object)((IFn)this.retrying_write).invoke((Object)const__6, (Object)new KVCluster$fn__10953$fn__10957$fn__10958(this.kvs, item, this.rev)))) {
                keyword = const__5;
            } else {
                IPersistentMap iPersistentMap2 = item;
                item = null;
                Object object = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap2, ((IFn)this.retrying_read).invoke((Object)const__6, (Object)new KVCluster$fn__10953$fn__10957$fn__10960(this.kvs, this.ref_key)));
                if (object != null && object != Boolean.FALSE) {
                    Logger logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
                    if (logger.isInfoEnabled()) {
                        Logger logger2 = logger;
                        logger = null;
                        logger2.info((String)((IFn)const__8.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, const__10, const__11, this.ref_key = null, const__2, this.rev = null})));
                    }
                    keyword = const__5;
                } else {
                    keyword = const__12;
                }
            }
            objectArray[1] = keyword;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__13;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

