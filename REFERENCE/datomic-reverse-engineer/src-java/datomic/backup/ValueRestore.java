/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.backup;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.IValueRestore;
import datomic.backup.ValueRestore$fn__20174;
import datomic.backup.ValueRestore$fn__20180;
import datomic.backup.ValueRestore$fn__20188;
import datomic.cluster.Get2;
import datomic.treewalk.NodeId;
import datomic.treewalk.TreeWalker;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValueRestore
implements IValueRestore,
IType {
    public final Object value_storage;
    public final Object to_cluster;
    public final Object progress;
    public final Object incremental_QMARK_;
    public final Object k__GT_backup_k;
    public final Object ids__GT_nodes;
    public final Object sem;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    public static final Keyword const__0;
    public static final Keyword const__1;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Var const__8;
    public static final Var const__9;
    public static final Keyword const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Keyword const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__27;
    public static final Var const__28;
    public static final Var const__29;
    public static final Var const__32;
    public static final Var const__33;
    public static final Var const__34;
    public static final Keyword const__35;
    public static final Var const__36;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;

    public ValueRestore(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.value_storage = object;
        this.to_cluster = object2;
        this.progress = object3;
        this.incremental_QMARK_ = object4;
        this.k__GT_backup_k = object5;
        this.ids__GT_nodes = object6;
        this.sem = object7;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"value-storage"), Symbol.intern(null, (String)"to-cluster"), Symbol.intern(null, (String)"progress"), Symbol.intern(null, (String)"incremental?"), Symbol.intern(null, (String)"k->backup-k"), Symbol.intern(null, (String)"ids->nodes"), ((IObj)Symbol.intern(null, (String)"sem")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Semaphore")}))});
    }

    /*
     * Unable to fully structure code
     */
    public Object restore_node(Object node) {
        block22: {
            block21: {
                block19: {
                    block20: {
                        block18: {
                            v0 = node;
                            if (Util.classOf((Object)v0) == ValueRestore.__cached_class__0) ** GOTO lbl6
                            if (!(v0 instanceof NodeId)) {
                                v0 = v0;
                                ValueRestore.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
                                // 2 sources

                                v1 = ValueRestore.const__16.getRawRoot().invoke(v0);
                            } else {
                                v1 = ((NodeId)v0).node_id();
                            }
                            k = v1;
                            ((IFn)this.k__GT_backup_k).invoke(k);
                            v2 = (IFn)ValueRestore.const__17.getRawRoot();
                            v3 = this.to_cluster;
                            if (Util.classOf((Object)v3) == ValueRestore.__cached_class__1) ** GOTO lbl18
                            if (!(v3 instanceof Get2)) {
                                v3 = v3;
                                ValueRestore.__cached_class__1 = Util.classOf((Object)v3);
lbl18:
                                // 2 sources

                                v4 = ValueRestore.const__18.getRawRoot().invoke(v3, k, (Object)ValueRestore.const__20);
                            } else {
                                v4 = ((Get2)v3).get_val2(k, ValueRestore.const__20);
                            }
                            v5 = and__5236__auto__20197 = (cluster_v = v2.invoke(v4));
                            if (v5 != null && v5 != Boolean.FALSE) {
                                v6 = this.incremental_QMARK_;
                            } else {
                                v6 = and__5236__auto__20197;
                                and__5236__auto__20197 = null;
                            }
                            if (v6 == null || v6 == Boolean.FALSE) break block18;
                            break block19;
                        }
                        v7 = node;
                        if (Util.classOf((Object)v7) == ValueRestore.__cached_class__2) ** GOTO lbl36
                        if (!(v7 instanceof TreeWalker)) {
                            v7 = v7;
                            ValueRestore.__cached_class__2 = Util.classOf((Object)v7);
lbl36:
                            // 2 sources

                            v8 = ValueRestore.const__21.getRawRoot().invoke(v7, this.ids__GT_nodes);
                        } else {
                            v8 = ((TreeWalker)v7).subtrees(this.ids__GT_nodes);
                        }
                        v9 = temp__5455__auto__20202 = v8;
                        if (v9 == null || v9 == Boolean.FALSE) break block20;
                        v10 = temp__5455__auto__20202;
                        temp__5455__auto__20202 = null;
                        v11 = branch_nodes = v10;
                        branch_nodes = null;
                        v12 = futs = ((IFn)ValueRestore.const__22.getRawRoot()).invoke((Object)new ValueRestore$fn__20180(this), v11);
                        futs = null;
                        seq_20184 = ((IFn)ValueRestore.const__23.getRawRoot()).invoke(v12);
                        chunk_20185 = null;
                        count_20186 = 0L;
                        i_20187 = 0L;
                        while (true) {
                            if (i_20187 < count_20186) {
                                v13 = fut = ((Indexed)chunk_20185).nth(RT.intCast((long)i_20187));
                                fut = null;
                                ((IFn)ValueRestore.const__17.getRawRoot()).invoke(v13);
                                v14 = seq_20184;
                                seq_20184 = null;
                                v15 = chunk_20185;
                                chunk_20185 = null;
                                ++i_20187;
                                chunk_20185 = v15;
                                seq_20184 = v14;
                                continue;
                            }
                            v16 = seq_20184;
                            seq_20184 = null;
                            v17 = temp__5457__auto__20199 = ((IFn)ValueRestore.const__23.getRawRoot()).invoke(v16);
                            if (v17 == null || v17 == Boolean.FALSE) break;
                            v18 = temp__5457__auto__20199;
                            temp__5457__auto__20199 = null;
                            seq_20184 = v18;
                            v19 = ((IFn)ValueRestore.const__27.getRawRoot()).invoke(seq_20184);
                            if (v19 != null && v19 != Boolean.FALSE) {
                                c__5719__auto__20198 = ((IFn)ValueRestore.const__28.getRawRoot()).invoke(seq_20184);
                                v20 = seq_20184;
                                seq_20184 = null;
                                v21 = c__5719__auto__20198;
                                v22 = c__5719__auto__20198;
                                c__5719__auto__20198 = null;
                                i_20187 = RT.intCast((long)0L);
                                count_20186 = RT.intCast((int)RT.count((Object)v22));
                                chunk_20185 = v21;
                                seq_20184 = ((IFn)ValueRestore.const__29.getRawRoot()).invoke(v20);
                                continue;
                            }
                            v23 = fut = ((IFn)ValueRestore.const__32.getRawRoot()).invoke(seq_20184);
                            fut = null;
                            ((IFn)ValueRestore.const__17.getRawRoot()).invoke(v23);
                            v24 = seq_20184;
                            seq_20184 = null;
                            i_20187 = 0L;
                            count_20186 = 0L;
                            chunk_20185 = null;
                            seq_20184 = ((IFn)ValueRestore.const__33.getRawRoot()).invoke(v24);
                        }
                        break block19;
                    }
                    v25 = (IFn)ValueRestore.const__22.getRawRoot();
                    v26 = new ValueRestore$fn__20188(this, this.progress, this.to_cluster);
                    v27 = node;
                    node = null;
                    v28 = v27;
                    if (Util.classOf((Object)v27) == ValueRestore.__cached_class__3) ** GOTO lbl108
                    if (!(v28 instanceof TreeWalker)) {
                        v28 = v28;
                        ValueRestore.__cached_class__3 = Util.classOf((Object)v28);
lbl108:
                        // 2 sources

                        v29 = ValueRestore.const__34.getRawRoot().invoke(v28);
                    } else {
                        v29 = ((TreeWalker)v28).child_node_ids();
                    }
                    v30 = futs = v25.invoke((Object)v26, v29);
                    futs = null;
                    seq_20192 = ((IFn)ValueRestore.const__23.getRawRoot()).invoke(v30);
                    chunk_20193 = null;
                    count_20194 = 0L;
                    i_20195 = 0L;
                    while (true) {
                        if (i_20195 < count_20194) {
                            v31 = fut = ((Indexed)chunk_20193).nth(RT.intCast((long)i_20195));
                            fut = null;
                            ((IFn)ValueRestore.const__17.getRawRoot()).invoke(v31);
                            v32 = seq_20192;
                            seq_20192 = null;
                            v33 = chunk_20193;
                            chunk_20193 = null;
                            ++i_20195;
                            chunk_20193 = v33;
                            seq_20192 = v32;
                            continue;
                        }
                        v34 = seq_20192;
                        seq_20192 = null;
                        v35 = temp__5457__auto__20201 = ((IFn)ValueRestore.const__23.getRawRoot()).invoke(v34);
                        if (v35 == null || v35 == Boolean.FALSE) break;
                        v36 = temp__5457__auto__20201;
                        temp__5457__auto__20201 = null;
                        seq_20192 = v36;
                        v37 = ((IFn)ValueRestore.const__27.getRawRoot()).invoke(seq_20192);
                        if (v37 != null && v37 != Boolean.FALSE) {
                            c__5719__auto__20200 = ((IFn)ValueRestore.const__28.getRawRoot()).invoke(seq_20192);
                            v38 = seq_20192;
                            seq_20192 = null;
                            v39 = c__5719__auto__20200;
                            v40 = c__5719__auto__20200;
                            c__5719__auto__20200 = null;
                            i_20195 = RT.intCast((long)0L);
                            count_20194 = RT.intCast((int)RT.count((Object)v40));
                            chunk_20193 = v39;
                            seq_20192 = ((IFn)ValueRestore.const__29.getRawRoot()).invoke(v38);
                            continue;
                        }
                        v41 = fut = ((IFn)ValueRestore.const__32.getRawRoot()).invoke(seq_20192);
                        fut = null;
                        ((IFn)ValueRestore.const__17.getRawRoot()).invoke(v41);
                        v42 = seq_20192;
                        seq_20192 = null;
                        i_20195 = 0L;
                        count_20194 = 0L;
                        chunk_20193 = null;
                        seq_20192 = ((IFn)ValueRestore.const__33.getRawRoot()).invoke(v42);
                    }
                }
                v43 = cluster_v;
                cluster_v = null;
                if (v43 == null || v43 == Boolean.FALSE) break block21;
                this = null;
                v44 = ((IFn)this.progress).invoke((Object)ValueRestore.const__35);
                break block22;
            }
            v45 = this;
            if (Util.classOf((Object)v45) == ValueRestore.__cached_class__4) ** GOTO lbl176
            if (!(v45 instanceof IValueRestore)) {
                v45 = v45;
                ValueRestore.__cached_class__4 = Util.classOf((Object)v45);
lbl176:
                // 2 sources

                v46 = k;
                k = null;
                this = null;
                v44 = ValueRestore.const__36.getRawRoot().invoke((Object)v45, v46);
            } else {
                v47 = k;
                k = null;
                v44 = ((IValueRestore)v45).restore_val(v47);
            }
        }
        return v44;
    }

    public Object restore_val(Object k) {
        Object object;
        ((Semaphore)this.sem).acquire();
        try {
            Object object2;
            IPersistentMap iPersistentMap;
            IPersistentMap m_20171 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, k});
            Logger logger = LoggerFactory.getLogger((String)"datomic.backup");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_20171, (Object)const__5, (Object)const__6)));
            }
            long start__8981__auto__20205 = System.nanoTime();
            Object object3 = k;
            k = null;
            Object result__8982__auto__20206 = ((IFn)new ValueRestore$fn__20174(this.value_storage, this.progress, this.k__GT_backup_k, this.to_cluster, object3)).invoke();
            long elapsed_20172 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20205);
            Object msec_20173 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20172));
            IFn iFn = (IFn)const__9.getRawRoot();
            IPersistentMap iPersistentMap2 = m_20171;
            m_20171 = null;
            Object object4 = msec_20173;
            msec_20173 = null;
            Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__10, object4, (Object)const__5, (Object)const__11);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = result__8982__auto__20206;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            if (object7 != null && object7 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__12;
                IFn iFn2 = (IFn)const__13.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object8 = result__8982__auto__20206;
                Object object9 = iLookupThunk2.get(object8);
                if (iLookupThunk2 == object9) {
                    __thunk__1__ = __site__1__.fault(object8);
                    object9 = __thunk__1__.get(object8);
                }
                objectArray[1] = iFn2.invoke(object9);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__20203 = iFn.invoke(object5, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.backup");
            if (logger3.isInfoEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object10 = endmsg__8984__auto__20203;
                endmsg__8984__auto__20203 = null;
                logger4.info((String)((IFn)const__3.getRawRoot()).invoke(object10));
            }
            Object object11 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__20206, (Object)const__15);
            if (object11 != null && object11 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object12 = result__8982__auto__20206;
                result__8982__auto__20206 = null;
                object2 = iLookupThunk3.get(object12);
                if (iLookupThunk3 == object2) {
                    __thunk__2__ = __site__2__.fault(object12);
                    object2 = __thunk__2__.get(object12);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object13 = result__8982__auto__20206;
                result__8982__auto__20206 = null;
                Object object14 = iLookupThunk4.get(object13);
                if (iLookupThunk4 == object14) {
                    __thunk__3__ = __site__3__.fault(object13);
                    object14 = __thunk__3__.get(object13);
                }
                throw (Throwable)object14;
            }
            object = object2;
        }
        finally {
            ((Semaphore)this.sem).release();
        }
        return object;
    }

    static {
        const__0 = RT.keyword(null, (String)"event");
        const__1 = RT.keyword((String)"restore", (String)"segment");
        const__2 = RT.keyword(null, (String)"k");
        const__3 = RT.var((String)"datomic.slf4j", (String)"process");
        const__4 = RT.var((String)"clojure.core", (String)"assoc");
        const__5 = RT.keyword(null, (String)"phase");
        const__6 = RT.keyword(null, (String)"begin");
        const__8 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__9 = RT.var((String)"clojure.core", (String)"merge");
        const__10 = RT.keyword(null, (String)"msec");
        const__11 = RT.keyword(null, (String)"end");
        const__12 = RT.keyword(null, (String)"threw");
        const__13 = RT.var((String)"clojure.core", (String)"class");
        const__14 = RT.var((String)"clojure.core", (String)"contains?");
        const__15 = RT.keyword(null, (String)"returned");
        const__16 = RT.var((String)"datomic.treewalk", (String)"node-id");
        const__17 = RT.var((String)"clojure.core", (String)"deref");
        const__18 = RT.var((String)"datomic.cluster", (String)"get-val2");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"datomic.core2.val-store.opts", (String)"skip-cache"), Boolean.TRUE});
        const__21 = RT.var((String)"datomic.treewalk", (String)"subtrees");
        const__22 = RT.var((String)"clojure.core", (String)"mapv");
        const__23 = RT.var((String)"clojure.core", (String)"seq");
        const__27 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__28 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__29 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__32 = RT.var((String)"clojure.core", (String)"first");
        const__33 = RT.var((String)"clojure.core", (String)"next");
        const__34 = RT.var((String)"datomic.treewalk", (String)"child-node-ids");
        const__35 = RT.keyword(null, (String)"skipped");
        const__36 = RT.var((String)"datomic.backup", (String)"restore-val");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__3__ = __site__3__;
    }
}

