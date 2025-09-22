/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.TransactorHornetConnector;

public final class connector$fn__21226$__GT_TransactorHornetConnector__21280
extends AFunction {
    public Object invoke(Object cluster_conf, Object transactor_endpoint2, Object hornet_factory) {
        Object object = cluster_conf;
        cluster_conf = null;
        Object object2 = transactor_endpoint2;
        transactor_endpoint2 = null;
        Object object3 = hornet_factory;
        hornet_factory = null;
        return new TransactorHornetConnector(object, object2, object3);
    }
}

