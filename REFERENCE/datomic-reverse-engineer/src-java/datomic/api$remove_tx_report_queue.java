/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Connection;

public final class api$remove_tx_report_queue
extends AFunction {
    public static Object invokeStatic(Object connection) {
        Object object = connection;
        connection = null;
        ((Connection)object).removeTxReportQueue();
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$remove_tx_report_queue.invokeStatic(object2);
    }
}

