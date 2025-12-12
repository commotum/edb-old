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
import datomic.db.AssignPartitions;
import datomic.db.LocalizeTempid;

public final class db$process_match_partition$fn__13727
extends AFunction {
    Object local_tempids;
    Object db;
    Object part_reqs;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;

    public db$process_match_partition$fn__13727(Object object, Object object2, Object object3) {
        this.local_tempids = object;
        this.db = object2;
        this.part_reqs = object3;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object _, Object id, Object primary_id) {
        v0 = (AssignPartitions)this.part_reqs;
        v1 = id;
        id = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == db$process_match_partition$fn__13727.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof LocalizeTempid)) {
            v2 = v2;
            db$process_match_partition$fn__13727.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = db$process_match_partition$fn__13727.const__0.getRawRoot().invoke(v2, this.db, null, this.local_tempids);
        } else {
            v3 = ((LocalizeTempid)v2).local_id(this.db, null, this.local_tempids);
        }
        v4 = primary_id;
        primary_id = null;
        v5 = v4;
        if (Util.classOf((Object)v4) == db$process_match_partition$fn__13727.__cached_class__1) ** GOTO lbl19
        if (!(v5 instanceof LocalizeTempid)) {
            v5 = v5;
            db$process_match_partition$fn__13727.__cached_class__1 = Util.classOf((Object)v5);
lbl19:
            // 2 sources

            v6 = db$process_match_partition$fn__13727.const__0.getRawRoot().invoke(v5, this.db, null, this.local_tempids);
        } else {
            v6 = ((LocalizeTempid)v5).local_id(this.db, null, this.local_tempids);
        }
        return v0.matchPart(v3, v6);
    }

    static {
        const__0 = RT.var((String)"datomic.db", (String)"local-id");
    }
}

