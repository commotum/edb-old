/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.cluster.Dbid;
import datomic.garbage$gc_deleted_db$fn__19885;
import datomic.garbage$gc_deleted_db$fn__19887;
import datomic.garbage$gc_deleted_db$status__19883;
import java.util.Date;

public final class garbage$gc_deleted_db
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    private static Class __cached_class__5;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__5;
    public static final Var const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Object const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Object const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Var const__21;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object system_cluster, Object db_cluster, Object status_callback) {
        block16: {
            block15: {
                block14: {
                    v0 = status_callback;
                    status_callback = null;
                    status = new garbage$gc_deleted_db$status__19883(v0);
                    olookup = ((IFn)garbage$gc_deleted_db.const__0.getRawRoot()).invoke(db_cluster);
                    v1 = temp__5457__auto__19890 = ((IFn)garbage$gc_deleted_db.const__1.getRawRoot()).invoke((Object)"datomic.gcStoragePaceMsec");
                    if (v1 != null && v1 != Boolean.FALSE) {
                        v2 = temp__5457__auto__19890;
                        temp__5457__auto__19890 = null;
                        v3 = n = v2;
                        n = null;
                        ((IFn)garbage$gc_deleted_db.const__2.getRawRoot()).invoke((Object)"Pacing with datomic.gcStoragePaceMsec =", v3);
                    }
                    v4 = temp__5457__auto__19891 = ((IFn)garbage$gc_deleted_db.const__3.getRawRoot()).invoke(db_cluster);
                    if (v4 == null || v4 == Boolean.FALSE) break block14;
                    v5 = temp__5457__auto__19891;
                    temp__5457__auto__19891 = null;
                    log_root_id = v5;
                    c = RT.count((Object)((IFn)garbage$gc_deleted_db.const__5.getRawRoot()).invoke(log_root_id, olookup, (Object)Boolean.TRUE));
                    v6 = log_root_id;
                    log_root_id = null;
                    ((IFn)garbage$gc_deleted_db.const__6.getRawRoot()).invoke((Object)new garbage$gc_deleted_db$fn__19885(db_cluster, c, (Object)status), garbage$gc_deleted_db.const__7, ((IFn)garbage$gc_deleted_db.const__8.getRawRoot()).invoke(garbage$gc_deleted_db.const__9, ((IFn)garbage$gc_deleted_db.const__5.getRawRoot()).invoke(v6, olookup, (Object)Boolean.TRUE)));
                    ((IFn)status).invoke((Object)"Deleted ", (Object)c, (Object)" of ", (Object)c, (Object)" log segments");
                    v7 = db_cluster;
                    if (Util.classOf((Object)v7) == garbage$gc_deleted_db.__cached_class__0) ** GOTO lbl30
                    if (!(v7 instanceof ClusteredStore)) {
                        v7 = v7;
                        garbage$gc_deleted_db.__cached_class__0 = Util.classOf((Object)v7);
lbl30:
                        // 2 sources

                        v8 = garbage$gc_deleted_db.const__10.getRawRoot().invoke(v7, ((IFn)garbage$gc_deleted_db.const__11.getRawRoot()).invoke(db_cluster));
                    } else {
                        v8 = ((ClusteredStore)v7).delete_reference(((IFn)garbage$gc_deleted_db.const__11.getRawRoot()).invoke(db_cluster));
                    }
                }
                v9 = garbage$gc_deleted_db.__thunk__0__;
                v10 = (IFn)garbage$gc_deleted_db.const__13.getRawRoot();
                v11 = db_cluster;
                if (Util.classOf((Object)v11) == garbage$gc_deleted_db.__cached_class__1) ** GOTO lbl41
                if (!(v11 instanceof ClusteredStore)) {
                    v11 = v11;
                    garbage$gc_deleted_db.__cached_class__1 = Util.classOf((Object)v11);
lbl41:
                    // 2 sources

                    v12 = garbage$gc_deleted_db.const__14.getRawRoot().invoke(v11, ((IFn)garbage$gc_deleted_db.const__15.getRawRoot()).invoke(db_cluster));
                } else {
                    v12 = ((ClusteredStore)v11).get_ref(((IFn)garbage$gc_deleted_db.const__15.getRawRoot()).invoke(db_cluster));
                }
                v13 = v10.invoke(v12);
                v14 = v9.get(v13);
                if (v9 == v14) {
                    garbage$gc_deleted_db.__thunk__0__ = garbage$gc_deleted_db.__site__0__.fault(v13);
                    v14 = garbage$gc_deleted_db.__thunk__0__.get(v13);
                }
                v15 = temp__5457__auto__19892 = v14;
                if (v15 == null || v15 == Boolean.FALSE) break block15;
                v16 = temp__5457__auto__19892;
                temp__5457__auto__19892 = null;
                index_root_id = v16;
                c = RT.count((Object)((IFn)garbage$gc_deleted_db.const__16.getRawRoot()).invoke(index_root_id, olookup, (Object)Boolean.TRUE));
                v17 = index_root_id;
                index_root_id = null;
                v18 = olookup;
                olookup = null;
                ((IFn)garbage$gc_deleted_db.const__6.getRawRoot()).invoke((Object)new garbage$gc_deleted_db$fn__19887(c, db_cluster, (Object)status), garbage$gc_deleted_db.const__7, ((IFn)garbage$gc_deleted_db.const__8.getRawRoot()).invoke(garbage$gc_deleted_db.const__17, ((IFn)garbage$gc_deleted_db.const__16.getRawRoot()).invoke(v17, v18, (Object)Boolean.TRUE)));
                ((IFn)status).invoke((Object)"Deleted ", (Object)c, (Object)" of ", (Object)c, (Object)" index segments");
                v19 = db_cluster;
                if (Util.classOf((Object)v19) == garbage$gc_deleted_db.__cached_class__2) ** GOTO lbl69
                if (!(v19 instanceof ClusteredStore)) {
                    v19 = v19;
                    garbage$gc_deleted_db.__cached_class__2 = Util.classOf((Object)v19);
lbl69:
                    // 2 sources

                    v20 = garbage$gc_deleted_db.const__10.getRawRoot().invoke(v19, ((IFn)garbage$gc_deleted_db.const__15.getRawRoot()).invoke(db_cluster));
                } else {
                    v20 = ((ClusteredStore)v19).delete_reference(((IFn)garbage$gc_deleted_db.const__15.getRawRoot()).invoke(db_cluster));
                }
            }
            v21 = (IFn)garbage$gc_deleted_db.const__18.getRawRoot();
            v22 = system_cluster;
            system_cluster = null;
            v23 = db_cluster;
            if (Util.classOf((Object)v23) == garbage$gc_deleted_db.__cached_class__3) ** GOTO lbl81
            if (!(v23 instanceof Dbid)) {
                v23 = v23;
                garbage$gc_deleted_db.__cached_class__3 = Util.classOf((Object)v23);
lbl81:
                // 2 sources

                v24 = garbage$gc_deleted_db.const__19.getRawRoot().invoke(v23);
            } else {
                v24 = ((Dbid)v23).dbId();
            }
            v21.invoke(v22, v24);
            v25 = (IFn)garbage$gc_deleted_db.const__13.getRawRoot();
            v26 = db_cluster;
            if (Util.classOf((Object)v26) == garbage$gc_deleted_db.__cached_class__4) ** GOTO lbl92
            if (!(v26 instanceof ClusteredStore)) {
                v26 = v26;
                garbage$gc_deleted_db.__cached_class__4 = Util.classOf((Object)v26);
lbl92:
                // 2 sources

                v27 = garbage$gc_deleted_db.const__14.getRawRoot().invoke(v26, ((IFn)garbage$gc_deleted_db.const__20.getRawRoot()).invoke(db_cluster));
            } else {
                v27 = ((ClusteredStore)v26).get_ref(((IFn)garbage$gc_deleted_db.const__20.getRawRoot()).invoke(db_cluster));
            }
            v28 = v25.invoke(v27);
            if (v28 == null || v28 == Boolean.FALSE) break block16;
            ((IFn)status).invoke((Object)"Deleting garbage segments.  This may take a while.");
            c = ((IFn)garbage$gc_deleted_db.const__21.getRawRoot()).invoke(db_cluster, (Object)new Date());
            v29 = status;
            status = null;
            v30 = c;
            c = null;
            ((IFn)v29).invoke((Object)"Deleting ", v30, (Object)" garbage segments");
            v31 = db_cluster;
            if (Util.classOf((Object)v31) == garbage$gc_deleted_db.__cached_class__5) ** GOTO lbl111
            if (!(v31 instanceof ClusteredStore)) {
                v31 = v31;
                garbage$gc_deleted_db.__cached_class__5 = Util.classOf((Object)v31);
lbl111:
                // 2 sources

                v32 = db_cluster;
                db_cluster = null;
                v33 = garbage$gc_deleted_db.const__10.getRawRoot().invoke(v31, ((IFn)garbage$gc_deleted_db.const__20.getRawRoot()).invoke(v32));
            } else {
                v34 = db_cluster;
                db_cluster = null;
                v33 = ((ClusteredStore)v31).delete_reference(((IFn)garbage$gc_deleted_db.const__20.getRawRoot()).invoke(v34));
            }
        }
        return Boolean.TRUE;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return garbage$gc_deleted_db.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.domain", (String)"deserializing-repairing-lookup");
        const__1 = RT.var((String)"datomic.config", (String)"property");
        const__2 = RT.var((String)"clojure.core", (String)"println");
        const__3 = RT.var((String)"datomic.log", (String)"root-id");
        const__5 = RT.var((String)"datomic.treewalk", (String)"log-tree-seq");
        const__6 = RT.var((String)"clojure.core", (String)"reduce");
        const__7 = 0L;
        const__8 = RT.var((String)"clojure.core", (String)"partition");
        const__9 = 1000L;
        const__10 = RT.var((String)"datomic.cluster", (String)"delete-reference");
        const__11 = RT.var((String)"datomic.log", (String)"tail-pod-key");
        const__13 = RT.var((String)"clojure.core", (String)"deref");
        const__14 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__15 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__16 = RT.var((String)"datomic.treewalk", (String)"index-tree-seq");
        const__17 = 1000L;
        const__18 = RT.var((String)"datomic.catalog", (String)"remove-deleted-database");
        const__19 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__20 = RT.var((String)"datomic.garbage", (String)"root-ref-key");
        const__21 = RT.var((String)"datomic.garbage", (String)"gc");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

