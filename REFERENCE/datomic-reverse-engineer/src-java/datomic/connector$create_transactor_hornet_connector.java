/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.connector.TransactorHornetConnector;

public final class connector$create_transactor_hornet_connector
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.connector", (String)"create-transactor-hornet-connector");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__2 = RT.var((String)"datomic.connector", (String)"create-hornet-factory");

    public static Object invokeStatic(Object cluster_conf, Object endpoint, Object ttl) {
        Object object = ttl;
        ttl = null;
        Object hornet_factory = ((IFn)const__2.getRawRoot()).invoke(endpoint, object);
        Object object2 = cluster_conf;
        cluster_conf = null;
        Object object3 = endpoint;
        endpoint = null;
        Object object4 = hornet_factory;
        hornet_factory = null;
        return new TransactorHornetConnector(object2, object3, object4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return connector$create_transactor_hornet_connector.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object cluster_conf, Object endpoint) {
        Object object = cluster_conf;
        cluster_conf = null;
        Object object2 = endpoint;
        endpoint = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, ((IFn)const__1.getRawRoot()).invoke((Object)"datomic.peerConnectionTTLMsec"));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return connector$create_transactor_hornet_connector.invokeStatic(object3, object4);
    }
}

