/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.Message
 *  org.apache.activemq.artemis.api.core.client.ClientProducer
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.queue.BlockingProducer;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class artemis_client$fressian_producer$reify__20867
implements BlockingProducer,
IFn,
IObj {
    final IPersistentMap __meta;
    Object hornet_session;
    Object hornet_producer;
    Object write_handlers;
    public static final Var const__0 = RT.var((String)"datomic.artemis-client", (String)"create-fressian-message");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public artemis_client$fressian_producer$reify__20867(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.hornet_session = object;
        this.hornet_producer = object2;
        this.write_handlers = object3;
    }

    public artemis_client$fressian_producer$reify__20867(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new artemis_client$fressian_producer$reify__20867(iPersistentMap, this.hornet_session, this.hornet_producer, this.write_handlers);
    }

    public Object put(Object obj) {
        Boolean bl;
        try {
            Object object = obj;
            obj = null;
            ((ClientProducer)this.hornet_producer).send((Message)((IFn)this).invoke(object));
            bl = Boolean.TRUE;
        }
        catch (Throwable t2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.artemis-client");
            Object t2 = null;
            Throwable ex = t2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__1.getRawRoot()).invoke((Object)"Hornet send failed"), ex);
                Logger logger2 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__2.getRawRoot()).invoke((Object)logger2, (Object)throwable);
            }
            bl = Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object obj) {
        Object object = obj;
        obj = null;
        artemis_client$fressian_producer$reify__20867 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.hornet_session, this_.write_handlers, object, (Object)Boolean.FALSE);
    }
}

