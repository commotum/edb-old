/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.apache.activemq.artemis.api.core.ActiveMQBuffer
 *  org.apache.activemq.artemis.api.core.client.ClientMessage
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.impl.peer.ActiveMQInputStream;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

public final class artemis_client$input_stream
extends AFunction {
    public static Object invokeStatic(Object msg) {
        ActiveMQBuffer buf;
        Object object = msg;
        msg = null;
        ActiveMQBuffer activeMQBuffer = buf = ((ClientMessage)object).getBodyBuffer();
        buf = null;
        return new ActiveMQInputStream(activeMQBuffer);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return artemis_client$input_stream.invokeStatic(object2);
    }
}

