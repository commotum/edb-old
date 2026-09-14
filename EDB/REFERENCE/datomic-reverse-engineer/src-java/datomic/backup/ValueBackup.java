/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
import datomic.backup.IValueBackup;
import datomic.backup.ValueBackup$fn__20091;
import datomic.backup.ValueBackup$fn__20097;
import datomic.backup.ValueBackup$fn__20099;
import datomic.backup.ValueBackup$fn__20107;
import datomic.treewalk.NodeId;
import datomic.treewalk.TreeWalker;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValueBackup
implements IValueBackup,
IType {
    public final Object from_cluster;
    public final Object value_storage;
    public final Object progress;
    public final Object incremental_QMARK_;
    public final Object ids__GT_nodes;
    public final Object throttle;
    public final Object sem;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
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
    public static final Var const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__24;
    public static final Var const__26;
    public static final Var const__27;
    public static final Var const__28;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final Keyword const__34;
    public static final Var const__35;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;

    public ValueBackup(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.from_cluster = object;
        this.value_storage = object2;
        this.progress = object3;
        this.incremental_QMARK_ = object4;
        this.ids__GT_nodes = object5;
        this.throttle = object6;
        this.sem = object7;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"from-cluster"), Symbol.intern(null, (String)"value-storage"), Symbol.intern(null, (String)"progress"), Symbol.intern(null, (String)"incremental?"), Symbol.intern(null, (String)"ids->nodes"), Symbol.intern(null, (String)"throttle"), ((IObj)Symbol.intern(null, (String)"sem")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Semaphore")}))});
    }

    /*
     * Unable to fully structure code
     */
    public Object backup_node(Object node) {
        block20: {
            block19: {
                block17: {
                    block18: {
                        block16: {
                            v0 = node;
                            if (Util.classOf((Object)v0) == ValueBackup.__cached_class__0) ** GOTO lbl6
                            if (!(v0 instanceof NodeId)) {
                                v0 = v0;
                                ValueBackup.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
                                // 2 sources

                                v1 = ValueBackup.const__16.getRawRoot().invoke(v0);
                            } else {
                                v1 = ((NodeId)v0).node_id();
                            }
                            k = v1;
                            backup_k = ((IFn)ValueBackup.const__17.getRawRoot()).invoke(k);
                            v2 = and__5236__auto__20118 = (node_exists_QMARK_ = ((IFn)ValueBackup.const__18.getRawRoot()).invoke((Object)new ValueBackup$fn__20097(this.value_storage, backup_k)));
                            if (v2 != null && v2 != Boolean.FALSE) {
                                v3 = this.incremental_QMARK_;
                            } else {
                                v3 = and__5236__auto__20118;
                                and__5236__auto__20118 = null;
                            }
                            if (v3 == null || v3 == Boolean.FALSE) break block16;
                            break block17;
                        }
                        v4 = node;
                        if (Util.classOf((Object)v4) == ValueBackup.__cached_class__1) ** GOTO lbl26
                        if (!(v4 instanceof TreeWalker)) {
                            v4 = v4;
                            ValueBackup.__cached_class__1 = Util.classOf((Object)v4);
lbl26:
                            // 2 sources

                            v5 = ValueBackup.const__19.getRawRoot().invoke(v4, this.ids__GT_nodes);
                        } else {
                            v5 = ((TreeWalker)v4).subtrees(this.ids__GT_nodes);
                        }
                        v6 = temp__5455__auto__20123 = v5;
                        if (v6 == null || v6 == Boolean.FALSE) break block18;
                        v7 = temp__5455__auto__20123;
                        temp__5455__auto__20123 = null;
                        v8 = branch_nodes = v7;
                        branch_nodes = null;
                        v9 = futs = ((IFn)ValueBackup.const__20.getRawRoot()).invoke((Object)new ValueBackup$fn__20099(this), v8);
                        futs = null;
                        seq_20103 = ((IFn)ValueBackup.const__21.getRawRoot()).invoke(v9);
                        chunk_20104 = null;
                        count_20105 = 0L;
                        i_20106 = 0L;
                        while (true) {
                            if (i_20106 < count_20105) {
                                v10 = fut = ((Indexed)chunk_20104).nth(RT.intCast((long)i_20106));
                                fut = null;
                                ((IFn)ValueBackup.const__24.getRawRoot()).invoke(v10);
                                v11 = seq_20103;
                                seq_20103 = null;
                                v12 = chunk_20104;
                                chunk_20104 = null;
                                ++i_20106;
                                chunk_20104 = v12;
                                seq_20103 = v11;
                                continue;
                            }
                            v13 = seq_20103;
                            seq_20103 = null;
                            v14 = temp__5457__auto__20120 = ((IFn)ValueBackup.const__21.getRawRoot()).invoke(v13);
                            if (v14 == null || v14 == Boolean.FALSE) break;
                            v15 = temp__5457__auto__20120;
                            temp__5457__auto__20120 = null;
                            seq_20103 = v15;
                            v16 = ((IFn)ValueBackup.const__26.getRawRoot()).invoke(seq_20103);
                            if (v16 != null && v16 != Boolean.FALSE) {
                                c__5719__auto__20119 = ((IFn)ValueBackup.const__27.getRawRoot()).invoke(seq_20103);
                                v17 = seq_20103;
                                seq_20103 = null;
                                v18 = c__5719__auto__20119;
                                v19 = c__5719__auto__20119;
                                c__5719__auto__20119 = null;
                                i_20106 = RT.intCast((long)0L);
                                count_20105 = RT.intCast((int)RT.count((Object)v19));
                                chunk_20104 = v18;
                                seq_20103 = ((IFn)ValueBackup.const__28.getRawRoot()).invoke(v17);
                                continue;
                            }
                            v20 = fut = ((IFn)ValueBackup.const__31.getRawRoot()).invoke(seq_20103);
                            fut = null;
                            ((IFn)ValueBackup.const__24.getRawRoot()).invoke(v20);
                            v21 = seq_20103;
                            seq_20103 = null;
                            i_20106 = 0L;
                            count_20105 = 0L;
                            chunk_20104 = null;
                            seq_20103 = ((IFn)ValueBackup.const__32.getRawRoot()).invoke(v21);
                        }
                        break block17;
                    }
                    v22 = (IFn)ValueBackup.const__20.getRawRoot();
                    v23 = new ValueBackup$fn__20107(this.throttle, this.progress, this.value_storage, this);
                    v24 = node;
                    node = null;
                    v25 = v24;
                    if (Util.classOf((Object)v24) == ValueBackup.__cached_class__2) ** GOTO lbl98
                    if (!(v25 instanceof TreeWalker)) {
                        v25 = v25;
                        ValueBackup.__cached_class__2 = Util.classOf((Object)v25);
lbl98:
                        // 2 sources

                        v26 = ValueBackup.const__33.getRawRoot().invoke(v25);
                    } else {
                        v26 = ((TreeWalker)v25).child_node_ids();
                    }
                    v27 = futs = v22.invoke((Object)v23, v26);
                    futs = null;
                    seq_20113 = ((IFn)ValueBackup.const__21.getRawRoot()).invoke(v27);
                    chunk_20114 = null;
                    count_20115 = 0L;
                    i_20116 = 0L;
                    while (true) {
                        if (i_20116 < count_20115) {
                            v28 = fut = ((Indexed)chunk_20114).nth(RT.intCast((long)i_20116));
                            fut = null;
                            ((IFn)ValueBackup.const__24.getRawRoot()).invoke(v28);
                            v29 = seq_20113;
                            seq_20113 = null;
                            v30 = chunk_20114;
                            chunk_20114 = null;
                            ++i_20116;
                            chunk_20114 = v30;
                            seq_20113 = v29;
                            continue;
                        }
                        v31 = seq_20113;
                        seq_20113 = null;
                        v32 = temp__5457__auto__20122 = ((IFn)ValueBackup.const__21.getRawRoot()).invoke(v31);
                        if (v32 == null || v32 == Boolean.FALSE) break;
                        v33 = temp__5457__auto__20122;
                        temp__5457__auto__20122 = null;
                        seq_20113 = v33;
                        v34 = ((IFn)ValueBackup.const__26.getRawRoot()).invoke(seq_20113);
                        if (v34 != null && v34 != Boolean.FALSE) {
                            c__5719__auto__20121 = ((IFn)ValueBackup.const__27.getRawRoot()).invoke(seq_20113);
                            v35 = seq_20113;
                            seq_20113 = null;
                            v36 = c__5719__auto__20121;
                            v37 = c__5719__auto__20121;
                            c__5719__auto__20121 = null;
                            i_20116 = RT.intCast((long)0L);
                            count_20115 = RT.intCast((int)RT.count((Object)v37));
                            chunk_20114 = v36;
                            seq_20113 = ((IFn)ValueBackup.const__28.getRawRoot()).invoke(v35);
                            continue;
                        }
                        v38 = fut = ((IFn)ValueBackup.const__31.getRawRoot()).invoke(seq_20113);
                        fut = null;
                        ((IFn)ValueBackup.const__24.getRawRoot()).invoke(v38);
                        v39 = seq_20113;
                        seq_20113 = null;
                        i_20116 = 0L;
                        count_20115 = 0L;
                        chunk_20114 = null;
                        seq_20113 = ((IFn)ValueBackup.const__32.getRawRoot()).invoke(v39);
                    }
                }
                v40 = node_exists_QMARK_;
                node_exists_QMARK_ = null;
                if (v40 == null || v40 == Boolean.FALSE) break block19;
                this = null;
                v41 = ((IFn)this.progress).invoke((Object)ValueBackup.const__34);
                break block20;
            }
            v42 = this;
            if (Util.classOf((Object)v42) == ValueBackup.__cached_class__3) ** GOTO lbl166
            if (!(v42 instanceof IValueBackup)) {
                v42 = v42;
                ValueBackup.__cached_class__3 = Util.classOf((Object)v42);
lbl166:
                // 2 sources

                v43 = k;
                k = null;
                v44 = backup_k;
                backup_k = null;
                this = null;
                v41 = ValueBackup.const__35.getRawRoot().invoke((Object)v42, v43, v44);
            } else {
                v45 = k;
                k = null;
                v46 = backup_k;
                backup_k = null;
                v41 = ((IValueBackup)v42).backup_val(v45, v46);
            }
        }
        return v41;
    }

    public Object backup_val(Object k, Object backup_k) {
        Object object;
        ((Semaphore)this.sem).acquire();
        try {
            Object object2;
            IPersistentMap iPersistentMap;
            IPersistentMap m_20088 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, k});
            Logger logger = LoggerFactory.getLogger((String)"datomic.backup");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_20088, (Object)const__5, (Object)const__6)));
            }
            long start__8981__auto__20126 = System.nanoTime();
            Object object3 = backup_k;
            backup_k = null;
            Object object4 = k;
            k = null;
            Object result__8982__auto__20127 = ((IFn)new ValueBackup$fn__20091(this.from_cluster, object3, this.progress, this.value_storage, object4)).invoke();
            long elapsed_20089 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20126);
            Object msec_20090 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20089));
            IFn iFn = (IFn)const__9.getRawRoot();
            IPersistentMap iPersistentMap2 = m_20088;
            m_20088 = null;
            Object object5 = msec_20090;
            msec_20090 = null;
            Object object6 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__10, object5, (Object)const__5, (Object)const__11);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object7 = result__8982__auto__20127;
            Object object8 = iLookupThunk.get(object7);
            if (iLookupThunk == object8) {
                __thunk__0__ = __site__0__.fault(object7);
                object8 = __thunk__0__.get(object7);
            }
            if (object8 != null && object8 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__12;
                IFn iFn2 = (IFn)const__13.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object9 = result__8982__auto__20127;
                Object object10 = iLookupThunk2.get(object9);
                if (iLookupThunk2 == object10) {
                    __thunk__1__ = __site__1__.fault(object9);
                    object10 = __thunk__1__.get(object9);
                }
                objectArray[1] = iFn2.invoke(object10);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__20124 = iFn.invoke(object6, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.backup");
            if (logger3.isInfoEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object11 = endmsg__8984__auto__20124;
                endmsg__8984__auto__20124 = null;
                logger4.info((String)((IFn)const__3.getRawRoot()).invoke(object11));
            }
            Object object12 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__20127, (Object)const__15);
            if (object12 != null && object12 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object13 = result__8982__auto__20127;
                result__8982__auto__20127 = null;
                object2 = iLookupThunk3.get(object13);
                if (iLookupThunk3 == object2) {
                    __thunk__2__ = __site__2__.fault(object13);
                    object2 = __thunk__2__.get(object13);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object14 = result__8982__auto__20127;
                result__8982__auto__20127 = null;
                Object object15 = iLookupThunk4.get(object14);
                if (iLookupThunk4 == object15) {
                    __thunk__3__ = __site__3__.fault(object14);
                    object15 = __thunk__3__.get(object14);
                }
                throw (Throwable)object15;
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
        const__1 = RT.keyword((String)"backup", (String)"segment");
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
        const__17 = RT.var((String)"datomic.backup", (String)"add-key-prefix");
        const__18 = RT.var((String)"datomic.backup", (String)"retry");
        const__19 = RT.var((String)"datomic.treewalk", (String)"subtrees");
        const__20 = RT.var((String)"clojure.core", (String)"mapv");
        const__21 = RT.var((String)"clojure.core", (String)"seq");
        const__24 = RT.var((String)"clojure.core", (String)"deref");
        const__26 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__27 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__28 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__31 = RT.var((String)"clojure.core", (String)"first");
        const__32 = RT.var((String)"clojure.core", (String)"next");
        const__33 = RT.var((String)"datomic.treewalk", (String)"child-node-ids");
        const__34 = RT.keyword(null, (String)"skipped");
        const__35 = RT.var((String)"datomic.backup", (String)"backup-val");
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

