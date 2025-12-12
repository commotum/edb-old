/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  org.apache.activemq.artemis.api.core.client.ClientSession
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import org.apache.activemq.artemis.api.core.client.ClientSession;

public final class artemis_client$create_message
extends AFunction {
    public static Object invokeStatic(Object session, Object message_type, Object durable, Object expiration, Object timestamp, Object priority) {
        Object object = session;
        session = null;
        Object object2 = message_type;
        message_type = null;
        Object object3 = durable;
        durable = null;
        Object object4 = expiration;
        expiration = null;
        Object object5 = timestamp;
        timestamp = null;
        Object object6 = priority;
        priority = null;
        return ((ClientSession)object).createMessage(RT.byteCast((Object)object2), RT.booleanCast((Object)object3), RT.longCast((Object)object4), RT.longCast((Object)object5), RT.byteCast((Object)object6));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return artemis_client$create_message.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    public static Object invokeStatic(Object session, Object message_type, Object durable) {
        Object object = session;
        session = null;
        Object object2 = message_type;
        message_type = null;
        Object object3 = durable;
        durable = null;
        return ((ClientSession)object).createMessage(RT.byteCast((Object)object2), RT.booleanCast((Object)object3));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return artemis_client$create_message.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object session, Object durable) {
        Object object = session;
        session = null;
        Object object2 = durable;
        durable = null;
        return ((ClientSession)object).createMessage(RT.booleanCast((Object)object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return artemis_client$create_message.invokeStatic(object3, object4);
    }
}

