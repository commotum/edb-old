/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.process_monitor$snapshot_metrics$fn__23495;

public final class process_monitor$snapshot_metrics
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.process-monitor", (String)"monitored-instances-ref");
    public static final Var const__3 = RT.var((String)"datomic.process-monitor", (String)"convert-nanos-to-millis");
    public static final Var const__4 = RT.var((String)"datomic.monitor", (String)"snapshot-statistics");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"second");

    public static Object invokeStatic() {
        Object statistic_data;
        Object status_map = ((IFn)const__0.getRawRoot()).invoke((Object)new process_monitor$snapshot_metrics$fn__23495(), ((IFn)const__1.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot())));
        Object object = statistic_data = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke());
        statistic_data = null;
        Object object2 = status_map;
        status_map = null;
        return ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), object, ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), object2));
    }

    public Object invoke() {
        return process_monitor$snapshot_metrics.invokeStatic();
    }
}

