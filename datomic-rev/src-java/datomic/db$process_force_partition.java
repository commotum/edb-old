/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$process_force_partition$fn__13724;

public final class db$process_force_partition
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce-kv");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"invalid-force-partition-map");
    public static final Keyword const__4 = RT.keyword(null, (String)"input");

    public static Object invokeStatic(Object forcemap, Object db2, Object part_reqs, Object local_tempids) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(forcemap);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            Object object4 = part_reqs;
            part_reqs = null;
            Object object5 = local_tempids;
            local_tempids = null;
            Object object6 = forcemap;
            forcemap = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)new db$process_force_partition$fn__13724(object3, object4, object5), null, object6);
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object7 = forcemap;
            forcemap = null;
            objectArray[1] = object7;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)"Value of :db/force-partition must be a map", (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$process_force_partition.invokeStatic(object5, object6, object7, object8);
    }
}

