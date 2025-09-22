/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup$create_backup_job$fn__20233;
import datomic.cluster.ClusteredStore;
import datomic.cluster.Dbid;

public final class backup$create_backup_job
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__6;
    public static final Var const__7;
    public static final Var const__10;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Keyword const__21;
    public static final Object const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Keyword const__25;
    public static final Keyword const__26;
    public static final Keyword const__27;
    public static final Keyword const__28;
    public static final Var const__29;
    public static final Keyword const__30;
    public static final Keyword const__31;
    public static final Keyword const__32;
    public static final Keyword const__33;
    public static final Keyword const__34;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object lookup) {
        vec__20230 = ((IFn)backup$create_backup_job.const__0.getRawRoot()).invoke(cluster);
        desc = RT.nth((Object)vec__20230, (int)RT.intCast((long)0L), null);
        v0 = vec__20230;
        vec__20230 = null;
        v1 = buf = RT.nth((Object)v0, (int)RT.intCast((long)1L), null);
        buf = null;
        log_tail = ((IFn)backup$create_backup_job.const__4.getRawRoot()).invoke(v1);
        tail_txes = ((IFn)backup$create_backup_job.const__5.getRawRoot()).invoke(log_tail, (Object)backup$create_backup_job.const__6, backup$create_backup_job.const__7.getRawRoot());
        v2 = backup$create_backup_job.__thunk__0__;
        v3 = desc;
        v4 = v2.get(v3);
        if (v2 == v4) {
            backup$create_backup_job.__thunk__0__ = backup$create_backup_job.__site__0__.fault(v3);
            v4 = backup$create_backup_job.__thunk__0__.get(v3);
        }
        log_root_id = v4;
        v5 = (IFn)backup$create_backup_job.const__10.getRawRoot();
        v6 = backup$create_backup_job.__thunk__1__;
        v7 = ((IFn)backup$create_backup_job.const__12.getRawRoot()).invoke(lookup, log_root_id);
        v8 = v6.get(v7);
        if (v6 == v8) {
            backup$create_backup_job.__thunk__1__ = backup$create_backup_job.__site__1__.fault(v7);
            v8 = backup$create_backup_job.__thunk__1__.get(v7);
        }
        v9 = v5.invoke(v8);
        v10 = (IFn)backup$create_backup_job.const__10.getRawRoot();
        v11 = backup$create_backup_job.__thunk__2__;
        v12 = tail_txes;
        tail_txes = null;
        v13 = ((IFn)backup$create_backup_job.const__13.getRawRoot()).invoke(v12);
        v14 = v11.get(v13);
        if (v11 == v14) {
            backup$create_backup_job.__thunk__2__ = backup$create_backup_job.__site__2__.fault(v13);
            v14 = backup$create_backup_job.__thunk__2__.get(v13);
        }
        t = Numbers.max((Object)v9, (Object)v10.invoke(v14));
        v15 = backup$create_backup_job.__thunk__3__;
        v16 = (IFn)backup$create_backup_job.const__15.getRawRoot();
        v17 = cluster;
        if (Util.classOf((Object)v17) == backup$create_backup_job.__cached_class__0) ** GOTO lbl44
        if (!(v17 instanceof ClusteredStore)) {
            v17 = v17;
            backup$create_backup_job.__cached_class__0 = Util.classOf((Object)v17);
lbl44:
            // 2 sources

            v18 = backup$create_backup_job.const__16.getRawRoot().invoke(v17, ((IFn)backup$create_backup_job.const__17.getRawRoot()).invoke(cluster));
        } else {
            v18 = ((ClusteredStore)v17).get_ref(((IFn)backup$create_backup_job.const__17.getRawRoot()).invoke(cluster));
        }
        v19 = v16.invoke(v18);
        v20 = v15.get(v19);
        if (v15 == v20) {
            backup$create_backup_job.__thunk__3__ = backup$create_backup_job.__site__3__.fault(v19);
            v20 = backup$create_backup_job.__thunk__3__.get(v19);
        }
        index_root_id = v20;
        index_root_map = ((IFn)backup$create_backup_job.const__18.getRawRoot()).invoke(lookup, index_root_id);
        index_top_node = ((IFn)backup$create_backup_job.const__19.getRawRoot()).invoke(index_root_id, backup$create_backup_job.const__20.getRawRoot(), lookup);
        v21 = new backup$create_backup_job$fn__20233(lookup);
        v22 = lookup;
        lookup = null;
        log_root_node = ((IFn)backup$create_backup_job.const__19.getRawRoot()).invoke(log_root_id, (Object)v21, v22);
        v23 = new Object[20];
        v23[0] = backup$create_backup_job.const__21;
        v23[1] = backup$create_backup_job.const__22;
        v23[2] = backup$create_backup_job.const__23;
        v24 = cluster;
        cluster = null;
        v25 = v24;
        if (Util.classOf((Object)v24) == backup$create_backup_job.__cached_class__1) ** GOTO lbl71
        if (!(v25 instanceof Dbid)) {
            v25 = v25;
            backup$create_backup_job.__cached_class__1 = Util.classOf((Object)v25);
lbl71:
            // 2 sources

            v26 = backup$create_backup_job.const__24.getRawRoot().invoke(v25);
        } else {
            v26 = ((Dbid)v25).dbId();
        }
        v23[3] = v26;
        v23[4] = backup$create_backup_job.const__25;
        v27 = log_root_node;
        log_root_node = null;
        v23[5] = v27;
        v23[6] = backup$create_backup_job.const__26;
        v28 = log_root_id;
        log_root_id = null;
        v23[7] = v28;
        v23[8] = backup$create_backup_job.const__27;
        v29 = index_top_node;
        index_top_node = null;
        v23[9] = v29;
        v23[10] = backup$create_backup_job.const__28;
        v30 = index_root_map;
        index_root_map = null;
        v23[11] = Numbers.num((long)((IFn.OL)backup$create_backup_job.const__29.getRawRoot()).invokePrim(v30));
        v23[12] = backup$create_backup_job.const__30;
        v31 = t;
        t = null;
        v23[13] = v31;
        v23[14] = backup$create_backup_job.const__31;
        v32 = index_root_id;
        index_root_id = null;
        v23[15] = v32;
        v23[16] = backup$create_backup_job.const__32;
        v33 = log_tail;
        log_tail = null;
        v23[17] = v33;
        v23[18] = backup$create_backup_job.const__33;
        v34 = desc;
        desc = null;
        v23[19] = ((IFn)backup$create_backup_job.const__18.getRawRoot()).invoke(v34, (Object)backup$create_backup_job.const__34);
        return RT.mapUniqueKeys((Object[])v23);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$create_backup_job.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.log", (String)"read-tail-descriptor");
        const__4 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");
        const__5 = RT.var((String)"datomic.fressian", (String)"defressian");
        const__6 = RT.keyword(null, (String)"handlers");
        const__7 = RT.var((String)"datomic.log", (String)"read-handlers");
        const__10 = RT.var((String)"datomic.log", (String)"max-t");
        const__12 = RT.var((String)"datomic.log", (String)"last-tree-tx");
        const__13 = RT.var((String)"clojure.core", (String)"last");
        const__15 = RT.var((String)"clojure.core", (String)"deref");
        const__16 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__17 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__18 = RT.var((String)"datomic.common", (String)"getx");
        const__19 = RT.var((String)"datomic.treewalk", (String)"create-parent-node");
        const__20 = RT.var((String)"datomic.treewalk", (String)"index-top-walker");
        const__21 = RT.keyword((String)"backup", (String)"version");
        const__22 = 3L;
        const__23 = RT.keyword(null, (String)"db-id");
        const__24 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__25 = RT.keyword(null, (String)"log-root-node");
        const__26 = RT.keyword(null, (String)"log-root-id");
        const__27 = RT.keyword(null, (String)"index-top-node");
        const__28 = RT.keyword((String)"index", (String)"version");
        const__29 = RT.var((String)"datomic.index", (String)"valid-version");
        const__30 = RT.keyword(null, (String)"t");
        const__31 = RT.keyword(null, (String)"index-root-id");
        const__32 = RT.keyword(null, (String)"log-tail");
        const__33 = RT.keyword((String)"log", (String)"version");
        const__34 = RT.keyword((String)"d", (String)"l");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__3__ = __site__3__;
    }
}

