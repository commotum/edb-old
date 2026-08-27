/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.monitor.Statistics;
import java.util.concurrent.ConcurrentHashMap;

public final class monitor$init_stats
extends AFunction {
    public static Object invokeStatic() {
        return new Statistics(new ConcurrentHashMap(), new ConcurrentHashMap(), new ConcurrentHashMap(), new ConcurrentHashMap());
    }

    public Object invoke() {
        return monitor$init_stats.invokeStatic();
    }
}

