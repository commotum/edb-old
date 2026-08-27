/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.ddb_s3_cluster$ensure_config$fn__22778;
import java.util.regex.Pattern;

public final class ddb_s3_cluster$ensure_config
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    public static final Keyword const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final AFn const__21;
    public static final Var const__22;
    public static final Keyword const__23;
    public static final Keyword const__24;
    public static final Var const__25;
    public static final Object const__26;
    public static final Keyword const__27;
    public static final Keyword const__30;
    public static final Object const__33;
    public static final Object const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Var const__38;
    public static final Var const__39;
    public static final Var const__40;
    public static final Keyword const__41;
    public static final Keyword const__42;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object p__22776) {
        block16: {
            block15: {
                v0 = p__22776;
                p__22776 = null;
                map__22777 = v0;
                v1 = ((IFn)ddb_s3_cluster$ensure_config.const__0.getRawRoot()).invoke(map__22777);
                if (v1 != null && v1 != Boolean.FALSE) {
                    v2 = map__22777;
                    map__22777 = null;
                    v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)ddb_s3_cluster$ensure_config.const__1.getRawRoot()).invoke(v2)));
                } else {
                    v3 = map__22777;
                    map__22777 = null;
                }
                map__22777 = v3;
                bucket = RT.get((Object)map__22777, (Object)ddb_s3_cluster$ensure_config.const__3);
                bucket_prefix = RT.get((Object)map__22777, (Object)ddb_s3_cluster$ensure_config.const__4);
                force = RT.get((Object)map__22777, (Object)ddb_s3_cluster$ensure_config.const__5, (Object)Boolean.FALSE);
                efs_path = RT.get((Object)map__22777, (Object)ddb_s3_cluster$ensure_config.const__6);
                region = RT.get((Object)map__22777, (Object)ddb_s3_cluster$ensure_config.const__7);
                system = RT.get((Object)map__22777, (Object)ddb_s3_cluster$ensure_config.const__8, (Object)"_default");
                v4 = map__22777;
                map__22777 = null;
                table_name = RT.get((Object)v4, (Object)ddb_s3_cluster$ensure_config.const__9);
                v5 = new Object[2];
                v5[0] = ddb_s3_cluster$ensure_config.const__7;
                v6 = region;
                region = null;
                v5[1] = v6;
                v7 = ddb_client = ((IFn)ddb_s3_cluster$ensure_config.const__10.getRawRoot()).invoke(null, ((IFn)ddb_s3_cluster$ensure_config.const__11.getRawRoot()).invoke(((IFn)ddb_s3_cluster$ensure_config.const__12.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v5)), (Object)ddb_s3_cluster$ensure_config.const__13, (Object)ddb_s3_cluster$ensure_config.const__14, (Object)ddb_s3_cluster$ensure_config.const__15, (Object)ddb_s3_cluster$ensure_config.const__16));
                ddb_client = null;
                v8 = table_name;
                table_name = null;
                v9 = system;
                system = null;
                v10 = kvs = ((IFn)ddb_s3_cluster$ensure_config.const__17.getRawRoot()).invoke(v7, v8, v9);
                kvs = null;
                cluster = ((IFn)ddb_s3_cluster$ensure_config.const__18.getRawRoot()).invoke(v10, (Object)ddb_s3_cluster$ensure_config.const__21);
                current_refval = ((IFn)ddb_s3_cluster$ensure_config.const__22.getRawRoot()).invoke(cluster);
                v11 = new Object[6];
                v11[0] = ddb_s3_cluster$ensure_config.const__23;
                v12 = bucket;
                bucket = null;
                v11[1] = v12;
                v11[2] = ddb_s3_cluster$ensure_config.const__24;
                v13 = bucket_prefix;
                bucket_prefix = null;
                v11[3] = ((IFn)ddb_s3_cluster$ensure_config.const__25.getRawRoot()).invoke(v13, ddb_s3_cluster$ensure_config.const__26, (Object)"");
                v11[4] = ddb_s3_cluster$ensure_config.const__27;
                v14 = efs_path;
                efs_path = null;
                v11[5] = v14;
                new_conf = RT.mapUniqueKeys((Object[])v11);
                v15 = ddb_s3_cluster$ensure_config.__thunk__0__;
                v16 = current_refval;
                v17 = v15.get(v16);
                if (v15 == v17) {
                    ddb_s3_cluster$ensure_config.__thunk__0__ = ddb_s3_cluster$ensure_config.__site__0__.fault(v16);
                    v17 = ddb_s3_cluster$ensure_config.__thunk__0__.get(v16);
                }
                if (!Util.equiv((Object)v17, (Object)new_conf)) break block15;
                v18 = new Object[2];
                v18[0] = ddb_s3_cluster$ensure_config.const__30;
                v19 = new_conf;
                new_conf = null;
                v18[1] = v19;
                v20 = RT.mapUniqueKeys((Object[])v18);
                break block16;
            }
            v21 = force;
            force = null;
            if (v21 != null && v21 != Boolean.FALSE) {
                v22 = ddb_s3_cluster$ensure_config.__thunk__1__;
                v23 = current_refval;
                current_refval = null;
                v24 = v22.get(v23);
                if (v22 == v24) {
                    ddb_s3_cluster$ensure_config.__thunk__1__ = ddb_s3_cluster$ensure_config.__site__1__.fault(v23);
                    v24 = ddb_s3_cluster$ensure_config.__thunk__1__.get(v23);
                }
                v25 = or__5238__auto__22781 = v24;
                if (v25 != null && v25 != Boolean.FALSE) {
                    v26 = or__5238__auto__22781;
                    or__5238__auto__22781 = null;
                } else {
                    v26 = ddb_s3_cluster$ensure_config.const__33;
                }
                v27 = Numbers.inc((Object)v26);
            } else {
                v28 = ddb_s3_cluster$ensure_config.__thunk__2__;
                v29 = current_refval;
                current_refval = null;
                v30 = v28.get(v29);
                if (v28 == v30) {
                    ddb_s3_cluster$ensure_config.__thunk__2__ = ddb_s3_cluster$ensure_config.__site__2__.fault(v29);
                    v30 = ddb_s3_cluster$ensure_config.__thunk__2__.get(v29);
                }
                v31 = or__5238__auto__22782 = v30;
                if (v31 != null && v31 != Boolean.FALSE) {
                    v27 = or__5238__auto__22782;
                    or__5238__auto__22782 = null;
                } else {
                    v27 = ddb_s3_cluster$ensure_config.const__34;
                }
            }
            rev = v27;
            v32 = (IFn)ddb_s3_cluster$ensure_config.const__35.getRawRoot();
            v33 = cluster;
            cluster = null;
            v34 = v33;
            if (Util.classOf((Object)v33) == ddb_s3_cluster$ensure_config.__cached_class__0) ** GOTO lbl110
            if (!(v34 instanceof ClusteredStore)) {
                v34 = v34;
                ddb_s3_cluster$ensure_config.__cached_class__0 = Util.classOf((Object)v34);
lbl110:
                // 2 sources

                v35 = rev;
                rev = null;
                ((IFn)ddb_s3_cluster$ensure_config.const__37.getRawRoot()).invoke(((IFn)ddb_s3_cluster$ensure_config.const__38.getRawRoot()).invoke((Object)ddb_s3_cluster$ensure_config.const__39, null, (Object)ddb_s3_cluster$ensure_config.const__40, null));
                v36 = ddb_s3_cluster$ensure_config.const__36.getRawRoot().invoke(v34, (Object)"ref-storage-config", v35, ((IFn)new ddb_s3_cluster$ensure_config$fn__22778(new_conf)).invoke());
            } else {
                v37 = (ClusteredStore)v34;
                v38 = rev;
                rev = null;
                ((IFn)ddb_s3_cluster$ensure_config.const__37.getRawRoot()).invoke(((IFn)ddb_s3_cluster$ensure_config.const__38.getRawRoot()).invoke((Object)ddb_s3_cluster$ensure_config.const__39, null, (Object)ddb_s3_cluster$ensure_config.const__40, null));
                v36 = v37.set_ref("ref-storage-config", v38, ((IFn)new ddb_s3_cluster$ensure_config$fn__22778(new_conf)).invoke());
            }
            result = v32.invoke(v36);
            if (Util.equiv((Object)ddb_s3_cluster$ensure_config.const__41, (Object)result)) {
                v39 = new Object[2];
                v39[0] = ddb_s3_cluster$ensure_config.const__30;
                v40 = new_conf;
                new_conf = null;
                v39[1] = v40;
                v20 = RT.mapUniqueKeys((Object[])v39);
            } else {
                v41 = new Object[2];
                v41[0] = ddb_s3_cluster$ensure_config.const__42;
                v42 = result;
                result = null;
                v41[1] = v42;
                v20 = RT.mapUniqueKeys((Object[])v41);
            }
        }
        return v20;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$ensure_config.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"bucket");
        const__4 = RT.keyword(null, (String)"bucket-prefix");
        const__5 = RT.keyword(null, (String)"force");
        const__6 = RT.keyword(null, (String)"efs-path");
        const__7 = RT.keyword(null, (String)"region");
        const__8 = RT.keyword(null, (String)"system");
        const__9 = RT.keyword(null, (String)"table-name");
        const__10 = RT.var((String)"datomic.ddb", (String)"client");
        const__11 = RT.var((String)"clojure.core", (String)"dissoc");
        const__12 = RT.var((String)"datomic.config", (String)"ddb-client-args");
        const__13 = RT.keyword(null, (String)"clientExecutionTimeout");
        const__14 = RT.keyword(null, (String)"connectionTimeout");
        const__15 = RT.keyword(null, (String)"requestTimeout");
        const__16 = RT.keyword(null, (String)"socketTimeout");
        const__17 = RT.var((String)"datomic.kv-dynamo", (String)"kv-dynamo");
        const__18 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"protocol"), RT.keyword(null, (String)"ddb+s3")});
        const__22 = RT.var((String)"datomic.ddb-s3-cluster", (String)"get-config-refval");
        const__23 = RT.keyword(null, (String)"s3-vals-bucket");
        const__24 = RT.keyword(null, (String)"s3-vals-prefix");
        const__25 = RT.var((String)"clojure.string", (String)"replace");
        const__26 = Pattern.compile("/$");
        const__27 = RT.keyword(null, (String)"fs-vals-path");
        const__30 = RT.keyword(null, (String)"success");
        const__33 = -1L;
        const__34 = 0L;
        const__35 = RT.var((String)"clojure.core", (String)"deref");
        const__36 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__37 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
        const__38 = RT.var((String)"clojure.core", (String)"hash-map");
        const__39 = RT.var((String)"clojure.core", (String)"*print-length*");
        const__40 = RT.var((String)"clojure.core", (String)"*print-level*");
        const__41 = RT.keyword(null, (String)"ok");
        const__42 = RT.keyword(null, (String)"failed");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
        __thunk__2__ = __site__2__;
    }
}

