/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.monitor.Metrics;

public final class process_monitor$snapshot_metrics$fn__23495
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object c) {
        Object object;
        Object object2 = c;
        Object object3 = c;
        c = null;
        Object object4 = object3;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object4 instanceof Metrics) {
                object = ((Metrics)object4).metrics();
                return Tuple.create((Object)object2, (Object)object);
            }
            object4 = object4;
            __cached_class__0 = Util.classOf((Object)object4);
        }
        object = const__0.getRawRoot().invoke(object4);
        return Tuple.create((Object)object2, (Object)object);
    }

    static {
        const__0 = RT.var((String)"datomic.monitor", (String)"metrics");
    }
}

