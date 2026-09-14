/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.Connection;

public final class peer$fn__21501$__GT_Connection__21574
extends AFunction {
    public Object invoke(Object db_id, Object cluster2, Object olookup, Object state_ref, Object db_ref, Object pending_txes, Object unsent_updates_queue, Object lucene_queue, Object tx_report_queue2, Object tx_watcher, Object idx_watcher, Object bg_watcher) {
        Object object = db_id;
        db_id = null;
        Object object2 = cluster2;
        cluster2 = null;
        Object object3 = olookup;
        olookup = null;
        Object object4 = state_ref;
        state_ref = null;
        Object object5 = db_ref;
        db_ref = null;
        Object object6 = pending_txes;
        pending_txes = null;
        Object object7 = unsent_updates_queue;
        unsent_updates_queue = null;
        Object object8 = lucene_queue;
        lucene_queue = null;
        Object object9 = tx_report_queue2;
        tx_report_queue2 = null;
        Object object10 = tx_watcher;
        tx_watcher = null;
        Object object11 = idx_watcher;
        idx_watcher = null;
        Object object12 = bg_watcher;
        bg_watcher = null;
        return new Connection(object, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12);
    }
}

