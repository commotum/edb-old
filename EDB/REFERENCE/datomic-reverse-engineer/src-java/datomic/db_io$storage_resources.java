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

public final class db_io$storage_resources
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"resolve-db-name");
    public static final Var const__2 = RT.var((String)"datomic.coordination", (String)"create-db-cluster");
    public static final Var const__3 = RT.var((String)"datomic.domain", (String)"system-cache-olookup");
    public static final Keyword const__4 = RT.keyword(null, (String)"cluster-conf");
    public static final Keyword const__5 = RT.keyword(null, (String)"resolved-conf");
    public static final Keyword const__6 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__7 = RT.keyword(null, (String)"olookup");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        Object cluster_conf = ((IFn)const__0.getRawRoot()).invoke(object);
        Object resolved_conf = ((IFn)const__1.getRawRoot()).invoke(cluster_conf);
        Object cluster2 = ((IFn)const__2.getRawRoot()).invoke(resolved_conf);
        Object olookup = ((IFn)const__3.getRawRoot()).invoke(cluster2);
        Object[] objectArray = new Object[8];
        objectArray[0] = const__4;
        Object object2 = cluster_conf;
        cluster_conf = null;
        objectArray[1] = object2;
        objectArray[2] = const__5;
        Object object3 = resolved_conf;
        resolved_conf = null;
        objectArray[3] = object3;
        objectArray[4] = const__6;
        Object object4 = cluster2;
        cluster2 = null;
        objectArray[5] = object4;
        objectArray[6] = const__7;
        Object object5 = olookup;
        olookup = null;
        objectArray[7] = object5;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db_io$storage_resources.invokeStatic(object2);
    }
}

