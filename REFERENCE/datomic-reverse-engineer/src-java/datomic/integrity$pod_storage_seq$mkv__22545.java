/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$pod_storage_seq$mkv__22545$fn__22546;
import datomic.kv_store.KVStore;

public final class integrity$pod_storage_seq$mkv__22545
extends AFunction {
    Object cluster;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final AFn const__4;
    public static final Keyword const__5;

    public integrity$pod_storage_seq$mkv__22545(Object object) {
        this.cluster = object;
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public Object invoke(Object id) {
        v0 = ((IFn)integrity$pod_storage_seq$mkv__22545.const__1.getRawRoot()).invoke(this.cluster);
        if (Util.classOf((Object)v0) == integrity$pod_storage_seq$mkv__22545.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof KVStore)) {
            v0 = v0;
            integrity$pod_storage_seq$mkv__22545.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = integrity$pod_storage_seq$mkv__22545.const__0.getRawRoot().invoke(v0, id, (Object)Boolean.FALSE);
        } else {
            v1 = ((KVStore)v0).get(id, Boolean.FALSE);
        }
        v2 = temp__5455__auto__22549 = v1;
        if (v2 != null && v2 != Boolean.FALSE) {
            v3 = temp__5455__auto__22549;
            temp__5455__auto__22549 = null;
            v4 = entry = v3;
            entry = null;
            this = null;
            v5 /* !! */  = ((IFn)integrity$pod_storage_seq$mkv__22545.const__2.getRawRoot()).invoke(v4, (Object)integrity$pod_storage_seq$mkv__22545.const__4, (Object)new integrity$pod_storage_seq$mkv__22545$fn__22546());
        } else {
            v6 = new Object[2];
            v6[0] = integrity$pod_storage_seq$mkv__22545.const__5;
            v7 = id;
            id = null;
            v6[1] = v7;
            v5 /* !! */  = RT.mapUniqueKeys((Object[])v6);
        }
        return v5 /* !! */ ;
    }

    static {
        const__0 = RT.var((String)"datomic.kv-store", (String)"get");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-ref-store");
        const__2 = RT.var((String)"clojure.core", (String)"update-in");
        const__4 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"v"));
        const__5 = RT.keyword(null, (String)"id");
    }
}

