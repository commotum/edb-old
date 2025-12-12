/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  org.apache.activemq.artemis.api.core.client.ClientConsumer
 *  org.apache.activemq.artemis.api.core.client.MessageHandler
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.artemis_client$set_handler$reify__20850;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.MessageHandler;

public final class artemis_client$set_handler
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 178, RT.keyword(null, (String)"column"), 4});

    public static Object invokeStatic(Object consumer, Object handler) {
        Object object = consumer;
        consumer = null;
        Object object2 = handler;
        handler = null;
        return ((ClientConsumer)object).setMessageHandler((MessageHandler)((IObj)new artemis_client$set_handler$reify__20850(null, object2)).withMeta((IPersistentMap)const__4));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return artemis_client$set_handler.invokeStatic(object3, object4);
    }
}

