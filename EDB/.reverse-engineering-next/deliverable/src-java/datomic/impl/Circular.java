/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.impl;

import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Util;

public class Circular {
    private static final Var EMAP = RT.var((String)"datomic.query", (String)"emap");
    private static final Var Q = RT.var((String)"datomic.query", (String)"q");
    private static final Var PULL_1 = RT.var((String)"datomic.pull", (String)"pull-1");
    private static final Var PULL = RT.var((String)"datomic.pull", (String)"pull");
    private static final Var INDEX_PULL = RT.var((String)"datomic.pull", (String)"dereffed-index-pull");
    private static final Var CACHE = RT.var((String)"datomic.query", (String)"cache");
    private static final Var CONSTRUCT_FN = RT.var((String)"datomic.query", (String)"construct-fn");
    private static final Var DB_STATS = RT.var((String)"datomic.stats", (String)"db-stats");

    public static Object emap(Object db2, Object eid) {
        return EMAP.invoke(db2, eid);
    }

    public static Object q(Object q2, Object srcs) {
        return Q.invoke(q2, srcs);
    }

    public static Object pull(Object db2, Object pattern, Object eid, Object options) {
        return PULL_1.invoke(db2, pattern, eid, options);
    }

    public static Object indexPull(Object db2, Object options) {
        return Util.streamOn((Iterable)INDEX_PULL.invoke(db2, options));
    }

    public static Object pullMany(Object db2, Object pattern, Object eids) {
        return PULL.invoke(db2, pattern, eids);
    }

    public static Object pullMany(Object db2, Object pattern, Object eids, Object options) {
        return PULL.invoke(db2, pattern, eids, options);
    }

    public static Object cache(Object q2) {
        return CACHE.invoke(q2);
    }

    public static Object constructFn(Object q2) {
        return CONSTRUCT_FN.invoke(q2);
    }

    public static Object dbStats(Object db2) {
        return DB_STATS.invoke(db2);
    }
}

