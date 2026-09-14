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
import datomic.connector.TransactorHornetConnector$fn__21230$fn__21231;

public final class TransactorHornetConnector$fn__21230
extends AFunction {
    Object session;

    public TransactorHornetConnector$fn__21230(Object object) {
        this.session = object;
    }

    public Object invoke() {
        ((IFn)new TransactorHornetConnector$fn__21230$fn__21231(this.session)).invoke();
        return null;
    }
}

