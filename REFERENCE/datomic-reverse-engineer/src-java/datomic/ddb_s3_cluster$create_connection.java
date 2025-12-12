/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb_s3_cluster$create_connection
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"skip-efs");
    public static final Var const__5 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-connection-without-efs");
    public static final Var const__6 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-connection-with-efs");

    public static Object invokeStatic(Object p__22801) {
        Object object;
        Object skip_efs;
        Object map__22802;
        Object object2;
        Object object3 = p__22801;
        p__22801 = null;
        Object map__228022 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__228022);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__228022;
            map__228022 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__228022;
            map__228022 = null;
        }
        Object cluster_conf = map__22802 = object2;
        Object object6 = map__22802;
        map__22802 = null;
        Object object7 = skip_efs = RT.get((Object)object6, (Object)const__3);
        skip_efs = null;
        if (RT.booleanCast((Object)object7)) {
            Object object8 = cluster_conf;
            cluster_conf = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object8);
        } else {
            Object object9 = cluster_conf;
            cluster_conf = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object9);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$create_connection.invokeStatic(object2);
    }
}

