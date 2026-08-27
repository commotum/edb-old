/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.aws_monitor.ToMetricData;

public final class aws_monitor$fn__23593$G__23589__23596
extends AFunction {
    public Object invoke(Object gf__v__23594, Object gf__k__23595) {
        Object object = gf__v__23594;
        gf__v__23594 = null;
        Object object2 = gf__k__23595;
        gf__k__23595 = null;
        return ((ToMetricData)object).to_metric_data_helper(object2);
    }
}

