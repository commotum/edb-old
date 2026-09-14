/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.apache.activemq.artemis.api.core.Message
 *  org.apache.activemq.artemis.api.core.client.ClientProducer
 */
package datomic.connector;

import clojure.lang.AFunction;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientProducer;

public final class TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21257
extends AFunction {
    Object hornet_producer;
    Object msg;

    public TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21257(Object object, Object object2) {
        this.hornet_producer = object;
        this.msg = object2;
    }

    public Object invoke() {
        Throwable throwable;
        try {
            ((ClientProducer)this.hornet_producer).send((Message)this.msg);
            throwable = null;
        }
        catch (Throwable e2) {
            Object e2 = null;
            throwable = e2;
        }
        return throwable;
    }
}

