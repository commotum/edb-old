/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.AssignPartitions;
import datomic.db.LocalizeTempid;

public final class db$process_force_partition$fn__13724
extends AFunction {
    Object db;
    Object part_reqs;
    Object local_tempids;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    public db$process_force_partition$fn__13724(Object object, Object object2, Object object3) {
        this.db = object;
        this.part_reqs = object2;
        this.local_tempids = object3;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object _, Object id, Object part) {
        v0 = (AssignPartitions)this.part_reqs;
        v1 = id;
        id = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == db$process_force_partition$fn__13724.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof LocalizeTempid)) {
            v2 = v2;
            db$process_force_partition$fn__13724.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = db$process_force_partition$fn__13724.const__0.getRawRoot().invoke(v2, this.db, null, this.local_tempids);
        } else {
            v3 = ((LocalizeTempid)v2).local_id(this.db, null, this.local_tempids);
        }
        v4 = part;
        part = null;
        return v0.forcePart(v3, ((IFn)db$process_force_partition$fn__13724.const__1.getRawRoot()).invoke(this.db, v4));
    }

    static {
        const__0 = RT.var((String)"datomic.db", (String)"local-id");
        const__1 = RT.var((String)"datomic.db", (String)"partbits");
    }
}

