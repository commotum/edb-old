/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.connector;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.connector.TransactorHornetConnector$fn__21238$fn__21239;

public final class TransactorHornetConnector$fn__21238
extends AFunction {
    Object hornet_consumer;
    Object unused;

    public TransactorHornetConnector$fn__21238(Object object, Object object2) {
        this.hornet_consumer = object;
        this.unused = object2;
    }

    public Object invoke() {
        ((IFn)new TransactorHornetConnector$fn__21238$fn__21239(this_.hornet_consumer)).invoke();
        TransactorHornetConnector$fn__21238 this_ = null;
        return ((IFn)this_.unused).invoke();
    }
}

