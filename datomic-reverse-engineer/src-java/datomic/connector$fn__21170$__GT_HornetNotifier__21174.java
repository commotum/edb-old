/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.HornetNotifier;

public final class connector$fn__21170$__GT_HornetNotifier__21174
extends AFunction {
    public Object invoke(Object push_handler_ref, Object session, Object result_queue, Object hornet_consumer, Object starter, Object cleanup2) {
        Object object = push_handler_ref;
        push_handler_ref = null;
        Object object2 = session;
        session = null;
        Object object3 = result_queue;
        result_queue = null;
        Object object4 = hornet_consumer;
        hornet_consumer = null;
        Object object5 = starter;
        starter = null;
        Object object6 = cleanup2;
        cleanup2 = null;
        return new HornetNotifier(object, object2, object3, object4, object5, object6);
    }
}

