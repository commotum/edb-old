/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.reconnector2.Reconnector;

public final class reconnector2$fn__17128$__GT_Reconnector__17141
extends AFunction {
    public Object invoke(Object current_promise_ref, Object worker_ref, Object shutdown_state, Object reconnect_fn, Object cleanup_fn) {
        Object object = current_promise_ref;
        current_promise_ref = null;
        Object object2 = worker_ref;
        worker_ref = null;
        Object object3 = shutdown_state;
        shutdown_state = null;
        Object object4 = reconnect_fn;
        reconnect_fn = null;
        Object object5 = cleanup_fn;
        cleanup_fn = null;
        return new Reconnector(object, object2, object3, object4, object5);
    }
}

