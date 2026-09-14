/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.monitor$snapshot_statistics$fn__541;
import datomic.monitor$snapshot_statistics$fn__547;
import datomic.monitor$snapshot_statistics$fn__553;
import datomic.monitor$snapshot_statistics$fn__559;
import datomic.monitor.Statistics;

public final class monitor$snapshot_statistics
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.monitor", (String)"statistics");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__3 = RT.var((String)"datomic.monitor", (String)"init-stats");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"merge-with");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic() {
        Object stats2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        ((IFn)const__2.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke());
        Object object = stats2;
        stats2 = null;
        return ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)new monitor$snapshot_statistics$fn__541(), (Object)PersistentArrayMap.EMPTY, ((Statistics)stats2).lo), ((IFn)const__6.getRawRoot()).invoke((Object)new monitor$snapshot_statistics$fn__547(), (Object)PersistentArrayMap.EMPTY, ((Statistics)stats2).hi), ((IFn)const__6.getRawRoot()).invoke((Object)new monitor$snapshot_statistics$fn__553(), (Object)PersistentArrayMap.EMPTY, ((Statistics)stats2).sum), ((IFn)const__6.getRawRoot()).invoke((Object)new monitor$snapshot_statistics$fn__559(), (Object)PersistentArrayMap.EMPTY, ((Statistics)object).count));
    }

    public Object invoke() {
        return monitor$snapshot_statistics.invokeStatic();
    }
}

