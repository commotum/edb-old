/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  org.apache.activemq.artemis.api.core.client.ClientMessage
 *  org.apache.activemq.artemis.api.core.client.MessageHandler
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.MessageHandler;

public final class artemis_client$set_handler$reify__20850
implements MessageHandler,
IObj {
    final IPersistentMap __meta;
    Object handler;

    public artemis_client$set_handler$reify__20850(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.handler = object;
    }

    public artemis_client$set_handler$reify__20850(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new artemis_client$set_handler$reify__20850(iPersistentMap, this.handler);
    }

    public void onMessage(ClientMessage msg) {
        ClientMessage clientMessage = msg;
        msg = null;
        artemis_client$set_handler$reify__20850 this_ = null;
        ((IFn)this_.handler).invoke((Object)clientMessage);
    }
}

