/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.apache.activemq.artemis.api.core.client.ClientConsumer
 *  org.apache.activemq.artemis.api.core.client.ClientMessage
 */
package datomic;

import clojure.lang.AFunction;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

public final class artemis_client$fn__20847
extends AFunction {
    public static Object invokeStatic(Object this_) {
        ClientMessage temp__5455__auto__20849;
        Object object = this_;
        this_ = null;
        ClientMessage clientMessage = temp__5455__auto__20849 = ((ClientConsumer)object).receive();
        if (clientMessage == null || clientMessage == Boolean.FALSE) {
            throw (Throwable)new InterruptedException();
        }
        ClientMessage clientMessage2 = temp__5455__auto__20849;
        temp__5455__auto__20849 = null;
        ClientMessage msg = clientMessage2;
        Object var2_2 = null;
        return msg;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return artemis_client$fn__20847.invokeStatic(object2);
    }
}

