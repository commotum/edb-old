/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.infinispan.client.hotrod.RemoteCacheManager
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.kv_hotrod$kv_infinispan$fn__17814;
import datomic.kv_hotrod.KVHotRod;
import org.infinispan.client.hotrod.RemoteCacheManager;

public final class kv_hotrod$kv_infinispan
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"port");
    public static final Var const__5 = RT.var((String)"datomic.kv-hotrod", (String)"managers");

    public static Object invokeStatic(Object endpoint) {
        Object manager;
        Object object;
        Object map__17813 = endpoint;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__17813);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__17813;
            map__17813 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object3)));
        } else {
            object = map__17813;
            map__17813 = null;
        }
        Object map__178132 = object;
        Object host = RT.get((Object)map__178132, (Object)const__3);
        Object object4 = map__178132;
        map__178132 = null;
        Object port = RT.get((Object)object4, (Object)const__4);
        Object lockee__5436__auto__17818 = const__5.getRawRoot();
        Object object5 = host;
        host = null;
        Object object6 = lockee__5436__auto__17818;
        lockee__5436__auto__17818 = null;
        Object object7 = port;
        port = null;
        Object object8 = endpoint;
        endpoint = null;
        Object object9 = manager = ((IFn)new kv_hotrod$kv_infinispan$fn__17814(object5, object6, object7, object8)).invoke();
        manager = null;
        return new KVHotRod(((RemoteCacheManager)object9).getCache("datomic"));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_hotrod$kv_infinispan.invokeStatic(object2);
    }
}

