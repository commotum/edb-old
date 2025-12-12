/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.peer.RemoteConnection;

public final class peer$create_connection$reconnect_fn__21588
extends AFunction {
    Object cluster_conf;
    Object system_cluster;
    Object pending_txes;
    Object unsent_updates_queue;
    Object endpoint_ref;
    Object conn;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final AFn const__9;
    public static final Var const__10;
    public static final Var const__11;

    public peer$create_connection$reconnect_fn__21588(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.cluster_conf = object;
        this.system_cluster = object2;
        this.pending_txes = object3;
        this.unsent_updates_queue = object4;
        this.endpoint_ref = object5;
        this.conn = object6;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object mode) {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke(this_.unsent_updates_queue, this_.pending_txes);
        Object new_endpoint = ((IFn)const__1.getRawRoot()).invoke(this_.system_cluster);
        if (Util.equiv((Object)new_endpoint, (Object)((IFn)const__3.getRawRoot()).invoke(this_.endpoint_ref))) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object2 = new_endpoint;
            new_endpoint = null;
            objectArray[1] = ((IFn)const__5.getRawRoot()).invoke(object2, (Object)const__9);
            object = RT.mapUniqueKeys((Object[])objectArray);
            return object;
        }
        ((IFn)const__10.getRawRoot()).invoke(this_.endpoint_ref, new_endpoint);
        Object object3 = this_.conn;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object3 instanceof RemoteConnection) {
                Object object4 = new_endpoint;
                new_endpoint = null;
                Object object5 = mode;
                mode = null;
                object = ((RemoteConnection)object3).create_connection_state(this_.cluster_conf, object4, object5);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object6 = new_endpoint;
        new_endpoint = null;
        Object object7 = mode;
        mode = null;
        peer$create_connection$reconnect_fn__21588 this_ = null;
        object = const__11.getRawRoot().invoke(object3, this_.cluster_conf, object6, object7);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.peer", (String)"fail-pending-txes");
        const__1 = RT.var((String)"datomic.coordination", (String)"lookup-compatible-transactor-endpoint");
        const__3 = RT.var((String)"clojure.core", (String)"deref");
        const__4 = RT.keyword(null, (String)"old-endpoint");
        const__5 = RT.var((String)"clojure.core", (String)"select-keys");
        const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"host"), (Object)RT.keyword(null, (String)"port"), (Object)RT.keyword(null, (String)"timestamp"));
        const__10 = RT.var((String)"clojure.core", (String)"reset!");
        const__11 = RT.var((String)"datomic.peer", (String)"create-connection-state");
    }
}

