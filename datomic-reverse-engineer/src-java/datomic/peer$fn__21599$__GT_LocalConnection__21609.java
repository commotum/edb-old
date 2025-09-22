/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.LocalConnection;

public final class peer$fn__21599$__GT_LocalConnection__21609
extends AFunction {
    public Object invoke(Object dbname, Object db_ref, Object tx_report_queue2, Object released, Object tx_watcher) {
        Object object = dbname;
        dbname = null;
        Object object2 = db_ref;
        db_ref = null;
        Object object3 = tx_report_queue2;
        tx_report_queue2 = null;
        Object object4 = released;
        released = null;
        Object object5 = tx_watcher;
        tx_watcher = null;
        return new LocalConnection(object, object2, object3, object4, object5);
    }
}

