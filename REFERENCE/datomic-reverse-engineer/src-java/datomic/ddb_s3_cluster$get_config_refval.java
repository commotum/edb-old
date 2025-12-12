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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class ddb_s3_cluster$get_config_refval
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__4;
    public static final Keyword const__5;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object kv_cluster) {
        v0 = (IFn)ddb_s3_cluster$get_config_refval.const__0.getRawRoot();
        v1 = kv_cluster;
        kv_cluster = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == ddb_s3_cluster$get_config_refval.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof ClusteredStore)) {
            v2 = v2;
            ddb_s3_cluster$get_config_refval.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = ddb_s3_cluster$get_config_refval.const__1.getRawRoot().invoke(v2, (Object)"ref-storage-config");
        } else {
            v3 = ((ClusteredStore)v2).get_ref("ref-storage-config");
        }
        refval = v0.invoke(v3);
        v4 = (IFn)ddb_s3_cluster$get_config_refval.const__2.getRawRoot();
        v5 = ddb_s3_cluster$get_config_refval.__thunk__0__;
        v6 = refval;
        v7 = v5.get(v6);
        if (v5 == v7) {
            ddb_s3_cluster$get_config_refval.__thunk__0__ = ddb_s3_cluster$get_config_refval.__site__0__.fault(v6);
            v7 = ddb_s3_cluster$get_config_refval.__thunk__0__.get(v6);
        }
        conf = v4.invoke(v7);
        v8 = new Object[4];
        v8[0] = ddb_s3_cluster$get_config_refval.const__4;
        v9 = conf;
        conf = null;
        v8[1] = v9;
        v8[2] = ddb_s3_cluster$get_config_refval.const__5;
        v10 = ddb_s3_cluster$get_config_refval.__thunk__1__;
        v11 = refval;
        refval = null;
        v12 = v10.get(v11);
        if (v10 == v12) {
            ddb_s3_cluster$get_config_refval.__thunk__1__ = ddb_s3_cluster$get_config_refval.__site__1__.fault(v11);
            v12 = ddb_s3_cluster$get_config_refval.__thunk__1__.get(v11);
        }
        v8[3] = v12;
        return RT.mapUniqueKeys((Object[])v8);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$get_config_refval.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__2 = RT.var((String)"clojure.edn", (String)"read-string");
        const__4 = RT.keyword(null, (String)"val");
        const__5 = RT.keyword(null, (String)"rev");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
        __thunk__1__ = __site__1__;
    }
}

