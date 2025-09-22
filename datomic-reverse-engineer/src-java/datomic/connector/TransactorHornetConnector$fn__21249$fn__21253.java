/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic.connector;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import datomic.connector.TransactorHornetConnector$fn__21249$fn__21253$fn__21254;

public final class TransactorHornetConnector$fn__21249$fn__21253
extends AFunction {
    Object handlers;
    Object hornet_producer;
    Object failure_handler;
    Object session;
    Object unsent_updates_queue;
    Object push_handler;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"threw");

    public TransactorHornetConnector$fn__21249$fn__21253(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.handlers = object;
        this.hornet_producer = object2;
        this.failure_handler = object3;
        this.session = object4;
        this.unsent_updates_queue = object5;
        this.push_handler = object6;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.handlers = null;
            this.hornet_producer = null;
            this.failure_handler = null;
            this.session = null;
            this.unsent_updates_queue = null;
            this.push_handler = null;
            objectArray[1] = ((IFn)new TransactorHornetConnector$fn__21249$fn__21253$fn__21254(this.handlers, this.hornet_producer, this.failure_handler, this.session, this.unsent_updates_queue, this.push_handler)).invoke();
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__1;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

