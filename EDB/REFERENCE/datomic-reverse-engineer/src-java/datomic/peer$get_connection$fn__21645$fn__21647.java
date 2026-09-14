/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class peer$get_connection$fn__21645$fn__21647
extends AFunction {
    Object resolved_cluster_conf;
    Object lockee__5436__auto__;
    public static final Var const__1 = RT.var((String)"datomic.peer", (String)"connection-cache");
    public static final Var const__2 = RT.var((String)"datomic.peer", (String)"create-connection");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"merge");
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"peer", (String)"cache-connection")});
    public static final Var const__8 = RT.var((String)"datomic.uri", (String)"loggable-cluster-conf");
    public static final Var const__9 = RT.var((String)"datomic.cache", (String)"put");

    public peer$get_connection$fn__21645$fn__21647(Object object, Object object2) {
        this.resolved_cluster_conf = object;
        this.lockee__5436__auto__ = object2;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Object invoke() {
        synchronized (this.lockee__5436__auto__) {
            Object object;
            Object conn;
            Object temp__5455__auto__21649;
            Object object2 = temp__5455__auto__21649 = RT.get((Object)const__1.getRawRoot(), (Object)this.resolved_cluster_conf);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = temp__5455__auto__21649;
                temp__5455__auto__21649 = null;
                object = conn = object3;
                conn = null;
            } else {
                conn = ((IFn)const__2.getRawRoot()).invoke(this.resolved_cluster_conf);
                Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    logger2.info((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke(this.resolved_cluster_conf))));
                }
                ((IFn)const__9.getRawRoot()).invoke(const__1.getRawRoot(), this.resolved_cluster_conf, conn);
                object = conn;
                Object var2_2 = null;
            }
            Object object4 = object;
            return object4;
        }
    }
}

