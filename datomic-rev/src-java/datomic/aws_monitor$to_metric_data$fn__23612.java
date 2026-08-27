/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.aws_monitor.ToMetricData;

public final class aws_monitor$to_metric_data$fn__23612
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__3;

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p__23611) {
        Object object;
        Object v;
        Object object2 = p__23611;
        p__23611 = null;
        Object vec__23613 = object2;
        Object k = RT.nth((Object)vec__23613, (int)RT.intCast((long)0L), null);
        Object object3 = vec__23613;
        vec__23613 = null;
        Object object4 = v = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        v = null;
        Object object5 = object4;
        if (Util.classOf((Object)object4) != __cached_class__0) {
            if (object5 instanceof ToMetricData) {
                Object object6 = k;
                k = null;
                object = ((ToMetricData)object5).to_metric_data_helper(object6);
                return object;
            }
            object5 = object5;
            __cached_class__0 = Util.classOf((Object)object5);
        }
        Object object7 = k;
        k = null;
        aws_monitor$to_metric_data$fn__23612 this_ = null;
        object = const__3.getRawRoot().invoke(object5, object7);
        return object;
    }

    static {
        const__3 = RT.var((String)"datomic.aws-monitor", (String)"to-metric-data-helper");
    }
}

