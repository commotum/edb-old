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
import datomic.db$process_match_partition$fn__13727;

public final class db$process_match_partition
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce-kv");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"invalid-match-partition-map");
    public static final Keyword const__4 = RT.keyword(null, (String)"input");

    public static Object invokeStatic(Object matchmap, Object db2, Object part_reqs, Object local_tempids) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(matchmap);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = local_tempids;
            local_tempids = null;
            Object object4 = db2;
            db2 = null;
            Object object5 = part_reqs;
            part_reqs = null;
            Object object6 = matchmap;
            matchmap = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)new db$process_match_partition$fn__13727(object3, object4, object5), null, object6);
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object7 = matchmap;
            matchmap = null;
            objectArray[1] = object7;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)"Value of :db/match-partition must be a map", (Object)RT.mapUniqueKeys((Object[])objectArray));
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
        return db$process_match_partition.invokeStatic(object5, object6, object7, object8);
    }
}

