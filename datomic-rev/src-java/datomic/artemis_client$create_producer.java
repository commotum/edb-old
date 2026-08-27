/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.apache.activemq.artemis.api.core.client.ClientSession
 */
package datomic;

import clojure.lang.AFunction;
import org.apache.activemq.artemis.api.core.client.ClientSession;

public final class artemis_client$create_producer
extends AFunction {
    public static Object invokeStatic(Object session, Object address) {
        Object object = session;
        session = null;
        Object object2 = address;
        address = null;
        return ((ClientSession)object).createProducer((String)object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return artemis_client$create_producer.invokeStatic(object3, object4);
    }
}

