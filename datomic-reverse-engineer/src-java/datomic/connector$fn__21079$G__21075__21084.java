/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.TransactorConnector;

public final class connector$fn__21079$G__21075__21084
extends AFunction {
    public Object invoke(Object gf_____21080, Object gf__update_queue__21081, Object gf__push_handler__21082, Object gf__failure_handler__21083) {
        Object object = gf_____21080;
        gf_____21080 = null;
        Object object2 = gf__update_queue__21081;
        gf__update_queue__21081 = null;
        Object object3 = gf__push_handler__21082;
        gf__push_handler__21082 = null;
        Object object4 = gf__failure_handler__21083;
        gf__failure_handler__21083 = null;
        return ((TransactorConnector)object).start_updater(object2, object3, object4);
    }
}

