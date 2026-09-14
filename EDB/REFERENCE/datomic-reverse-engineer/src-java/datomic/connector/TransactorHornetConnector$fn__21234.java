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
import datomic.connector.TransactorHornetConnector$fn__21234$fn__21235;

public final class TransactorHornetConnector$fn__21234
extends AFunction {
    Object unused;
    Object session;
    Object result_queue;

    public TransactorHornetConnector$fn__21234(Object object, Object object2, Object object3) {
        this.unused = object;
        this.session = object2;
        this.result_queue = object3;
    }

    public Object invoke() {
        ((IFn)new TransactorHornetConnector$fn__21234$fn__21235(this_.session, this_.result_queue)).invoke();
        TransactorHornetConnector$fn__21234 this_ = null;
        return ((IFn)this_.unused).invoke();
    }
}

