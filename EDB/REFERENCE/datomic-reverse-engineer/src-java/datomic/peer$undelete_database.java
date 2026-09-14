/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class peer$undelete_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Var const__3 = RT.var((String)"datomic.catalog", (String)"undelete-database");
    public static final Keyword const__4 = RT.keyword(null, (String)"no-db-name");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db_id, Object uri2) {
        Object object;
        Object db_name;
        Object cluster_conf = ((IFn)const__0.getRawRoot()).invoke(uri2);
        Object cluster2 = ((IFn)const__1.getRawRoot()).invoke(cluster_conf);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = cluster_conf;
        cluster_conf = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = db_name = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = cluster2;
            cluster2 = null;
            Object object6 = db_id;
            db_id = null;
            Object object7 = db_name;
            db_name = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object5, object6, object7);
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object8 = uri2;
            uri2 = null;
            objectArray[1] = object8;
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$undelete_database.invokeStatic(object3, object4);
    }
}

