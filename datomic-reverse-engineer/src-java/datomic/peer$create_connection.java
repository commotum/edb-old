/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.peer$create_connection$fn__21590;
import datomic.peer$create_connection$fn__21595;
import datomic.peer$create_connection$reconnect_fn__21588;
import datomic.peer.Connection;
import java.util.concurrent.ArrayBlockingQueue;

public final class peer$create_connection
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.peer", (String)"start-kv-cache-delay");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"promise");
    public static final Var const__5 = RT.var((String)"datomic.coordination", (String)"create-db-cluster");
    public static final Var const__6 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Var const__7 = RT.var((String)"datomic.domain", (String)"system-cache-olookup");
    public static final Var const__8 = RT.var((String)"datomic.cache", (String)"create-response-map");
    public static final Object const__9 = 60L;
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__11 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__12 = RT.keyword(null, (String)"db-id");
    public static final Var const__13 = RT.var((String)"datomic.peer", (String)"create-t-watcher");
    public static final Keyword const__14 = RT.keyword(null, (String)"basisT");
    public static final Keyword const__15 = RT.keyword(null, (String)"indexBasisT");
    public static final Var const__16 = RT.var((String)"datomic.math", (String)"create-exponential");
    public static final AFn const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"x1"), 1L, RT.keyword(null, (String)"y1"), 1000L, RT.keyword(null, (String)"x2"), 10L, RT.keyword(null, (String)"y2"), 120000L});
    public static final Keyword const__26 = RT.keyword(null, (String)"initial");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"deliver");
    public static final Var const__28 = RT.var((String)"datomic.reconnector2", (String)"reconnector-ref");
    public static final Keyword const__29 = RT.keyword(null, (String)"state");
    public static final Keyword const__30 = RT.keyword(null, (String)"reconnect");
    public static final Keyword const__31 = RT.keyword(null, (String)"cleanup");
    public static final Var const__32 = RT.var((String)"datomic.common", (String)"async-shutdown");
    public static final Keyword const__33 = RT.keyword(null, (String)"shutdown-state");
    public static final Keyword const__34 = RT.keyword((String)"datomic.peer", (String)"shutdown");

    public static Object invokeStatic(Object cluster_conf) {
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        ArrayBlockingQueue unsent_updates_queue = new ArrayBlockingQueue(RT.intCast((long)128L));
        ArrayBlockingQueue lucene_queue = new ArrayBlockingQueue(RT.intCast((long)128L));
        Object state_ref = ((IFn)const__4.getRawRoot()).invoke();
        Object cluster2 = ((IFn)const__5.getRawRoot()).invoke(cluster_conf);
        Object system_cluster2 = ((IFn)const__6.getRawRoot()).invoke(cluster_conf);
        Object olookup = ((IFn)const__7.getRawRoot()).invoke(cluster2);
        Object pending_txes = ((IFn)const__8.getRawRoot()).invoke(const__9);
        Object db_ref = ((IFn)const__10.getRawRoot()).invoke(null);
        Object object = cluster2;
        cluster2 = null;
        Object object2 = olookup;
        olookup = null;
        Connection conn = new Connection(((IFn)const__11.getRawRoot()).invoke(cluster_conf, (Object)const__12), object, object2, state_ref, db_ref, pending_txes, unsent_updates_queue, lucene_queue, ((IFn)const__10.getRawRoot()).invoke(null), ((IFn)const__13.getRawRoot()).invoke((Object)const__14, db_ref), ((IFn)const__13.getRawRoot()).invoke((Object)const__15, db_ref), ((IFn)const__13.getRawRoot()).invoke((Object)const__15, db_ref));
        Object endpoint_ref = ((IFn)const__10.getRawRoot()).invoke(null);
        ((IFn)const__16.getRawRoot()).invoke((Object)const__25);
        Object object3 = cluster_conf;
        cluster_conf = null;
        Object object4 = system_cluster2;
        system_cluster2 = null;
        Object object5 = pending_txes;
        pending_txes = null;
        ArrayBlockingQueue arrayBlockingQueue = unsent_updates_queue;
        unsent_updates_queue = null;
        Object object6 = endpoint_ref;
        endpoint_ref = null;
        peer$create_connection$reconnect_fn__21588 reconnect_fn = new peer$create_connection$reconnect_fn__21588(object3, object4, object5, arrayBlockingQueue, object6, conn);
        Object state2 = ((IFn)reconnect_fn).invoke((Object)const__26);
        Object object7 = state_ref;
        state_ref = null;
        Object object8 = state2;
        state2 = null;
        peer$create_connection$reconnect_fn__21588 peer$create_connection$reconnect_fn__21588 = reconnect_fn;
        reconnect_fn = null;
        ((IFn)const__27.getRawRoot()).invoke(object7, ((IFn)const__28.getRawRoot()).invoke((Object)const__29, object8, (Object)const__30, (Object)new peer$create_connection$fn__21590((Object)peer$create_connection$reconnect_fn__21588), (Object)const__31, const__32.getRawRoot(), (Object)const__33, (Object)const__34));
        ArrayBlockingQueue arrayBlockingQueue2 = lucene_queue;
        lucene_queue = null;
        Object object9 = db_ref;
        db_ref = null;
        Thread G__21594 = new Thread((Runnable)((Object)new peer$create_connection$fn__21595(arrayBlockingQueue2, object9)), "Datomic Fulltext Integration");
        G__21594.setDaemon(Boolean.TRUE);
        G__21594.start();
        Connection connection = conn;
        conn = null;
        return connection;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$create_connection.invokeStatic(object2);
    }
}

