/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  org.apache.activemq.artemis.api.core.ActiveMQException
 *  org.apache.activemq.artemis.api.core.client.SessionFailureListener
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.SessionFailureListener;

public final class artemis_client$wrap_as_failure_listener$reify__20801
implements SessionFailureListener,
IObj {
    final IPersistentMap __meta;
    Object f;

    public artemis_client$wrap_as_failure_listener$reify__20801(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.f = object;
    }

    public artemis_client$wrap_as_failure_listener$reify__20801(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new artemis_client$wrap_as_failure_listener$reify__20801(iPersistentMap, this.f);
    }

    public void connectionFailed(ActiveMQException ex, boolean failed_over, String scale_down_target_node_id) {
        ActiveMQException activeMQException = ex;
        ex = null;
        artemis_client$wrap_as_failure_listener$reify__20801 this_ = null;
        ((IFn)this_.f).invoke((Object)activeMQException);
    }

    public void connectionFailed(ActiveMQException ex, boolean failed_over) {
        ActiveMQException activeMQException = ex;
        ex = null;
        artemis_client$wrap_as_failure_listener$reify__20801 this_ = null;
        ((IFn)this_.f).invoke((Object)activeMQException);
    }

    public void beforeReconnect(ActiveMQException ex) {
    }
}

