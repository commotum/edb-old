/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset.IDataSet;
import datomic.cluster.AsyncWriter;
import datomic.db.Db;
import datomic.db.IndexSet;
import datomic.index$merge_db_STAR_$build_tiered_index__15765;
import datomic.index$merge_db_STAR_$fn__15852;
import datomic.index$merge_db_STAR_$fn__15854;
import datomic.index$merge_db_STAR_$fn__15856;
import datomic.index$merge_db_STAR_$fn__15858;
import org.slf4j.LoggerFactory;

public final class index$merge_db_STAR_
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Object const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Var const__14;
    public static final Keyword const__17;
    public static final AFn const__18;
    public static final Keyword const__19;
    public static final Var const__20;
    public static final Keyword const__21;
    public static final Keyword const__22;
    public static final Var const__23;
    public static final Object const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final Var const__28;
    public static final Keyword const__30;
    public static final AFn const__31;
    public static final Keyword const__32;
    public static final Keyword const__33;
    public static final Keyword const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final Keyword const__38;
    public static final AFn const__39;
    public static final Keyword const__40;
    public static final Keyword const__41;
    public static final Keyword const__42;
    public static final Var const__43;
    public static final Var const__44;
    public static final AFn const__47;
    public static final Var const__48;
    public static final Var const__49;
    public static final Var const__50;
    public static final Var const__51;
    public static final Var const__52;
    public static final Var const__53;
    public static final Keyword const__54;
    public static final Keyword const__55;
    public static final Var const__56;
    public static final Var const__57;
    public static final Keyword const__59;
    public static final AFn const__60;
    public static final Keyword const__61;
    public static final Keyword const__62;
    public static final Keyword const__63;
    public static final Var const__64;
    public static final Var const__65;
    public static final Object const__66;
    public static final Var const__68;
    public static final Var const__69;
    public static final Var const__70;
    public static final Var const__71;
    public static final Var const__72;
    public static final Var const__73;
    public static final Var const__74;
    public static final Var const__75;
    public static final Var const__76;
    public static final Var const__77;
    public static final Keyword const__78;
    public static final Keyword const__79;
    public static final Keyword const__80;
    public static final Keyword const__81;
    public static final Keyword const__82;
    public static final Keyword const__84;
    public static final Keyword const__85;
    public static final Var const__86;
    public static final Keyword const__87;
    public static final Keyword const__88;
    public static final Keyword const__89;
    public static final Keyword const__90;
    public static final Keyword const__91;
    public static final Keyword const__92;
    public static final Var const__93;
    public static final Keyword const__95;
    public static final Var const__97;
    public static final Keyword const__98;
    public static final Keyword const__99;
    public static final Var const__100;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;
    static final KeywordLookupSite __site__4__;
    static ILookupThunk __thunk__4__;
    static final KeywordLookupSite __site__5__;
    static ILookupThunk __thunk__5__;
    static final KeywordLookupSite __site__6__;
    static ILookupThunk __thunk__6__;
    static final KeywordLookupSite __site__7__;
    static ILookupThunk __thunk__7__;

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static Object invokeStatic(Object cstore, Object olookup, Object db, Object as_of_t, Object old_root_id, Object extra, Object fulltext_enabled_QMARK_, Object excise_enabled_QMARK_) {
        block23: {
            block21: {
                block22: {
                    job_started = System.nanoTime();
                    segs_written_ref = ((IFn)index$merge_db_STAR_.const__0.getRawRoot()).invoke(index$merge_db_STAR_.const__1);
                    dirs_written_ref = ((IFn)index$merge_db_STAR_.const__0.getRawRoot()).invoke(index$merge_db_STAR_.const__1);
                    root_map = ((IFn)index$merge_db_STAR_.const__2.getRawRoot()).invoke(olookup, old_root_id);
                    v0 = temp__5455__auto__15861 = ((IFn)index$merge_db_STAR_.const__3.getRawRoot()).invoke(db);
                    if (v0 != null && v0 != Boolean.FALSE) {
                        v1 = temp__5455__auto__15861;
                        temp__5455__auto__15861 = null;
                        attrids = v1;
                        v2 = root_map;
                        root_map = null;
                        v3 = attrids;
                        attrids = null;
                        v4 /* !! */  = ((IFn)index$merge_db_STAR_.const__4.getRawRoot()).invoke(cstore, olookup, db, as_of_t, v2, v3);
                    } else {
                        v5 = root_map;
                        root_map = null;
                        v4 /* !! */  = Tuple.create((Object)v5, (Object)PersistentVector.EMPTY);
                    }
                    vec__15741 = v4 /* !! */ ;
                    root_map = RT.nth((Object)vec__15741, (int)RT.uncheckedIntCast((long)0L), null);
                    v6 = vec__15741;
                    vec__15741 = null;
                    garbage = RT.nth((Object)v6, (int)RT.uncheckedIntCast((long)1L), null);
                    v7 = excise_enabled_QMARK_;
                    excise_enabled_QMARK_ = null;
                    xents = v7 != null && v7 != Boolean.FALSE ? ((IFn)index$merge_db_STAR_.const__7.getRawRoot()).invoke(db) : null;
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isInfoEnabled()) {
                        v8 = logger;
                        logger = null;
                        v8.info((String)((IFn)index$merge_db_STAR_.const__8.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{index$merge_db_STAR_.const__9, index$merge_db_STAR_.const__10, index$merge_db_STAR_.const__11, as_of_t, index$merge_db_STAR_.const__12, RT.count((Object)xents)})));
                    }
                    v9 = xents;
                    xents = null;
                    xpreds = ((IFn)index$merge_db_STAR_.const__14.getRawRoot()).invoke(db, v9);
                    v10 = garbage;
                    garbage = null;
                    build_tiered_index = new index$merge_db_STAR_$build_tiered_index__15765(db, as_of_t, root_map, v10, xpreds, cstore, dirs_written_ref, segs_written_ref, olookup);
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isInfoEnabled()) {
                        v11 = logger;
                        logger = null;
                        v11.info((String)((IFn)index$merge_db_STAR_.const__8.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__18));
                    }
                    eavt_ret = ((IFn)index$merge_db_STAR_.const__0.getRawRoot()).invoke(((IFn)build_tiered_index).invoke((Object)index$merge_db_STAR_.const__17, (Object)index$merge_db_STAR_.const__19, ((IFn)index$merge_db_STAR_.const__20.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__21, (Object)index$merge_db_STAR_.const__17), (Object)index$merge_db_STAR_.const__22, ((IFn)index$merge_db_STAR_.const__23.getRawRoot()).invoke(index$merge_db_STAR_.const__24), ((IFn)index$merge_db_STAR_.const__25.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__17), ((IndexSet)((Db)db).indexing).eavt, ((IndexSet)((Db)db).mid_index).eavt, ((IndexSet)((Db)db).index).eavt, index$merge_db_STAR_.const__26.getRawRoot(), index$merge_db_STAR_.const__27.getRawRoot(), index$merge_db_STAR_.const__28.getRawRoot(), (Object)index$merge_db_STAR_.const__17));
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isInfoEnabled()) {
                        v12 = logger;
                        logger = null;
                        v12.info((String)((IFn)index$merge_db_STAR_.const__8.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__31));
                    }
                    avet_ret = ((IFn)index$merge_db_STAR_.const__0.getRawRoot()).invoke(((IFn)build_tiered_index).invoke((Object)index$merge_db_STAR_.const__30, (Object)index$merge_db_STAR_.const__32, ((IFn)index$merge_db_STAR_.const__20.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__33, (Object)index$merge_db_STAR_.const__30), (Object)index$merge_db_STAR_.const__34, (Object)new index$merge_db_STAR_$fn__15852(), ((IFn)index$merge_db_STAR_.const__25.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__30), ((IndexSet)((Db)db).indexing).avet, ((IndexSet)((Db)db).mid_index).avet, ((IndexSet)((Db)db).index).avet, index$merge_db_STAR_.const__35.getRawRoot(), index$merge_db_STAR_.const__36.getRawRoot(), index$merge_db_STAR_.const__28.getRawRoot(), (Object)index$merge_db_STAR_.const__30));
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isInfoEnabled()) {
                        v13 = logger;
                        logger = null;
                        v13.info((String)((IFn)index$merge_db_STAR_.const__8.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__39));
                    }
                    aevt_ret = ((IFn)index$merge_db_STAR_.const__0.getRawRoot()).invoke(((IFn)build_tiered_index).invoke((Object)index$merge_db_STAR_.const__38, (Object)index$merge_db_STAR_.const__40, ((IFn)index$merge_db_STAR_.const__20.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__41, (Object)index$merge_db_STAR_.const__38), (Object)index$merge_db_STAR_.const__42, (Object)new index$merge_db_STAR_$fn__15854(), ((IFn)index$merge_db_STAR_.const__25.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__38), ((IndexSet)((Db)db).indexing).aevt, ((IndexSet)((Db)db).mid_index).aevt, ((IndexSet)((Db)db).index).aevt, index$merge_db_STAR_.const__43.getRawRoot(), index$merge_db_STAR_.const__44.getRawRoot(), index$merge_db_STAR_.const__28.getRawRoot(), (Object)index$merge_db_STAR_.const__38));
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isInfoEnabled()) {
                        v14 = logger;
                        logger = null;
                        v14.info((String)((IFn)index$merge_db_STAR_.const__8.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__47));
                    }
                    v15 = fulltext_enabled_QMARK_;
                    fulltext_enabled_QMARK_ = null;
                    if (v15 == null || v15 == Boolean.FALSE) break block21;
                    v16 = pario = ((IFn)index$merge_db_STAR_.const__48.getRawRoot()).invoke((Object)"datomic.indexIOParallelism");
                    cstore = v16 != null && v16 != Boolean.FALSE ? ((IFn)index$merge_db_STAR_.const__49.getRawRoot()).invoke(cstore, pario, index$merge_db_STAR_.const__50.getRawRoot(), (Object)new index$merge_db_STAR_$fn__15856()) : cstore;
                    v17 = (IFn)index$merge_db_STAR_.const__0.getRawRoot();
                    v18 = (IFn)index$merge_db_STAR_.const__51.getRawRoot();
                    v19 = ((IndexSet)((Db)db).indexing).aevt;
                    v20 = ((IFn)index$merge_db_STAR_.const__52.getRawRoot()).invoke((Object)new index$merge_db_STAR_$fn__15858(db), ((IFn)index$merge_db_STAR_.const__53.getRawRoot()).invoke(db));
                    v21 = index$merge_db_STAR_.__thunk__0__;
                    v22 = root_map;
                    v23 = v21.get(v22);
                    if (v21 == v23) {
                        index$merge_db_STAR_.__thunk__0__ = index$merge_db_STAR_.__site__0__.fault(v22);
                        v23 = index$merge_db_STAR_.__thunk__0__.get(v22);
                    }
                    v24 = index$merge_db_STAR_.__thunk__1__;
                    v25 = root_map;
                    root_map = null;
                    v26 = v24.get(v25);
                    if (v24 == v26) {
                        index$merge_db_STAR_.__thunk__1__ = index$merge_db_STAR_.__site__1__.fault(v25);
                        v26 = index$merge_db_STAR_.__thunk__1__.get(v25);
                    }
                    ret = v17.invoke(v18.invoke(cstore, olookup, db, v19, v20, v23, v26));
                    v27 = pario;
                    pario = null;
                    if (v27 == null || v27 == Boolean.FALSE) break block22;
                    v28 = (IFn)index$merge_db_STAR_.const__56.getRawRoot();
                    v29 = cstore;
                    cstore = null;
                    v30 = v29;
                    if (Util.classOf((Object)v29) == index$merge_db_STAR_.__cached_class__0) ** GOTO lbl102
                    if (!(v30 instanceof AsyncWriter)) {
                        v30 = v30;
                        index$merge_db_STAR_.__cached_class__0 = Util.classOf((Object)v30);
lbl102:
                        // 2 sources

                        v31 = index$merge_db_STAR_.const__57.getRawRoot().invoke(v30);
                    } else {
                        v31 = ((AsyncWriter)v30).finish_writer();
                    }
                    v28.invoke(v31, index$merge_db_STAR_.const__50.getRawRoot());
                }
                v32 = ret;
                ret = null;
                break block23;
            }
            v33 = (IFn)index$merge_db_STAR_.const__0.getRawRoot();
            v34 = index$merge_db_STAR_.__thunk__2__;
            v35 = root_map;
            v36 = v34.get(v35);
            if (v34 == v36) {
                index$merge_db_STAR_.__thunk__2__ = index$merge_db_STAR_.__site__2__.fault(v35);
                v36 = index$merge_db_STAR_.__thunk__2__.get(v35);
            }
            v37 = index$merge_db_STAR_.__thunk__3__;
            v38 = root_map;
            root_map = null;
            v39 = v37.get(v38);
            if (v37 == v39) {
                index$merge_db_STAR_.__thunk__3__ = index$merge_db_STAR_.__site__3__.fault(v38);
                v39 = index$merge_db_STAR_.__thunk__3__.get(v38);
            }
            v32 = v33.invoke((Object)Tuple.create((Object)v36, (Object)v39, null));
        }
        fulltext_ret = v32;
        logger = LoggerFactory.getLogger((String)"datomic.index");
        if (logger.isInfoEnabled()) {
            v40 = logger;
            logger = null;
            v40.info((String)((IFn)index$merge_db_STAR_.const__8.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__60));
        }
        v41 = build_tiered_index;
        build_tiered_index = null;
        vec__15744 = ((IFn)v41).invoke((Object)index$merge_db_STAR_.const__59, (Object)index$merge_db_STAR_.const__61, ((IFn)index$merge_db_STAR_.const__20.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__62, (Object)index$merge_db_STAR_.const__59), (Object)index$merge_db_STAR_.const__63, ((IFn)index$merge_db_STAR_.const__23.getRawRoot()).invoke(index$merge_db_STAR_.const__24), ((IFn)index$merge_db_STAR_.const__25.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__59), ((IndexSet)((Db)db).indexing).raet, ((IndexSet)((Db)db).mid_index).raet, ((IndexSet)((Db)db).index).raet, index$merge_db_STAR_.const__64.getRawRoot(), index$merge_db_STAR_.const__65.getRawRoot(), index$merge_db_STAR_.const__28.getRawRoot(), (Object)index$merge_db_STAR_.const__59);
        mid_raetid = RT.nth((Object)vec__15744, (int)RT.uncheckedIntCast((long)0L), null);
        raetid = RT.nth((Object)vec__15744, (int)RT.uncheckedIntCast((long)1L), null);
        hist_raetid = RT.nth((Object)vec__15744, (int)RT.uncheckedIntCast((long)2L), null);
        v42 = vec__15744;
        vec__15744 = null;
        garbage = RT.nth((Object)v42, (int)RT.uncheckedIntCast((long)3L), null);
        v43 = eavt_ret;
        eavt_ret = null;
        vec__15747 = ((IFn)index$merge_db_STAR_.const__68.getRawRoot()).invoke(v43);
        mid_eavtid = RT.nth((Object)vec__15747, (int)RT.uncheckedIntCast((long)0L), null);
        eavtid = RT.nth((Object)vec__15747, (int)RT.uncheckedIntCast((long)1L), null);
        hist_eavtid = RT.nth((Object)vec__15747, (int)RT.uncheckedIntCast((long)2L), null);
        v44 = vec__15747;
        vec__15747 = null;
        eavt_garbage = RT.nth((Object)v44, (int)RT.uncheckedIntCast((long)3L), null);
        v45 = avet_ret;
        avet_ret = null;
        vec__15750 = ((IFn)index$merge_db_STAR_.const__68.getRawRoot()).invoke(v45);
        mid_avetid = RT.nth((Object)vec__15750, (int)RT.uncheckedIntCast((long)0L), null);
        avetid = RT.nth((Object)vec__15750, (int)RT.uncheckedIntCast((long)1L), null);
        hist_avetid = RT.nth((Object)vec__15750, (int)RT.uncheckedIntCast((long)2L), null);
        v46 = vec__15750;
        vec__15750 = null;
        avet_garbage = RT.nth((Object)v46, (int)RT.uncheckedIntCast((long)3L), null);
        v47 = temp__5455__auto__15862 = ((IFn)index$merge_db_STAR_.const__69.getRawRoot()).invoke(((IFn)index$merge_db_STAR_.const__70.getRawRoot()).invoke(db));
        if (v47 != null && v47 != Boolean.FALSE) {
            v48 = temp__5455__auto__15862;
            temp__5455__auto__15862 = null;
            attrids = v48;
            v49 = olookup;
            olookup = null;
            v50 = mid_avetid;
            mid_avetid = null;
            v51 = avetid;
            avetid = null;
            v52 = hist_avetid;
            hist_avetid = null;
            v53 = attrids;
            attrids = null;
            v54 = avet_garbage;
            avet_garbage = null;
            v55 /* !! */  = ((IFn)index$merge_db_STAR_.const__71.getRawRoot()).invoke(cstore, v49, (Object)Tuple.create((Object)v50, (Object)v51, (Object)v52), v53, as_of_t, v54);
        } else {
            v56 = mid_avetid;
            mid_avetid = null;
            v57 = avetid;
            avetid = null;
            v58 = hist_avetid;
            hist_avetid = null;
            v59 = avet_garbage;
            avet_garbage = null;
            v55 /* !! */  = Tuple.create((Object)Tuple.create((Object)v56, (Object)v57, (Object)v58), (Object)v59);
        }
        vec__15753 = v55 /* !! */ ;
        vec__15756 = RT.nth((Object)vec__15753, (int)RT.uncheckedIntCast((long)0L), null);
        mid_avetid = RT.nth((Object)vec__15756, (int)RT.uncheckedIntCast((long)0L), null);
        avetid = RT.nth((Object)vec__15756, (int)RT.uncheckedIntCast((long)1L), null);
        v60 = vec__15756;
        vec__15756 = null;
        hist_avetid = RT.nth((Object)v60, (int)RT.uncheckedIntCast((long)2L), null);
        v61 = vec__15753;
        vec__15753 = null;
        avet_garbage = RT.nth((Object)v61, (int)RT.uncheckedIntCast((long)1L), null);
        v62 = aevt_ret;
        aevt_ret = null;
        vec__15759 = ((IFn)index$merge_db_STAR_.const__68.getRawRoot()).invoke(v62);
        mid_aevtid = RT.nth((Object)vec__15759, (int)RT.uncheckedIntCast((long)0L), null);
        aevtid = RT.nth((Object)vec__15759, (int)RT.uncheckedIntCast((long)1L), null);
        hist_aevtid = RT.nth((Object)vec__15759, (int)RT.uncheckedIntCast((long)2L), null);
        v63 = vec__15759;
        vec__15759 = null;
        aevt_garbage = RT.nth((Object)v63, (int)RT.uncheckedIntCast((long)3L), null);
        v64 = fulltext_ret;
        fulltext_ret = null;
        vec__15762 = ((IFn)index$merge_db_STAR_.const__68.getRawRoot()).invoke(v64);
        fulltextid = RT.nth((Object)vec__15762, (int)RT.uncheckedIntCast((long)0L), null);
        hist_fulltextid = RT.nth((Object)vec__15762, (int)RT.uncheckedIntCast((long)1L), null);
        v65 = vec__15762;
        vec__15762 = null;
        v66 = fulltext_garbage_keys = RT.nth((Object)v65, (int)RT.uncheckedIntCast((long)2L), null);
        fulltext_garbage_keys = null;
        v67 = garbage;
        garbage = null;
        v68 = eavt_garbage;
        eavt_garbage = null;
        v69 = avet_garbage;
        avet_garbage = null;
        v70 = aevt_garbage;
        aevt_garbage = null;
        garbage = ((IFn)index$merge_db_STAR_.const__72.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)index$merge_db_STAR_.const__73.getRawRoot()).invoke(v66, ((IFn)index$merge_db_STAR_.const__52.getRawRoot()).invoke(index$merge_db_STAR_.const__74.getRawRoot(), ((IFn)index$merge_db_STAR_.const__73.getRawRoot()).invoke(v67, v68, v69, v70))));
        rootid = ((IFn)index$merge_db_STAR_.const__75.getRawRoot()).invoke();
        v71 = (IFn)index$merge_db_STAR_.const__76.getRawRoot();
        v72 = (IFn)index$merge_db_STAR_.const__77.getRawRoot();
        v73 = extra;
        extra = null;
        v74 = new Object[42];
        v74[0] = index$merge_db_STAR_.const__78;
        v75 = index$merge_db_STAR_.__thunk__4__;
        v76 = db;
        v77 = v75.get(v76);
        if (v75 == v77) {
            index$merge_db_STAR_.__thunk__4__ = index$merge_db_STAR_.__site__4__.fault(v76);
            v77 = index$merge_db_STAR_.__thunk__4__.get(v76);
        }
        v74[1] = v77;
        v74[2] = index$merge_db_STAR_.const__79;
        v74[3] = Numbers.num((long)((Db)db).nextT());
        v74[4] = index$merge_db_STAR_.const__80;
        v78 = index$merge_db_STAR_.__thunk__5__;
        v79 = db;
        v80 = v78.get(v79);
        if (v78 == v80) {
            index$merge_db_STAR_.__thunk__5__ = index$merge_db_STAR_.__site__5__.fault(v79);
            v80 = index$merge_db_STAR_.__thunk__5__.get(v79);
        }
        v74[5] = v80;
        v74[6] = index$merge_db_STAR_.const__21;
        v81 = eavtid;
        eavtid = null;
        v74[7] = v81;
        v74[8] = index$merge_db_STAR_.const__81;
        v74[9] = Numbers.num((long)((Db)db).basisT());
        v74[10] = index$merge_db_STAR_.const__55;
        v82 = hist_fulltextid;
        hist_fulltextid = null;
        v74[11] = v82;
        v74[12] = index$merge_db_STAR_.const__54;
        v83 = fulltextid;
        fulltextid = null;
        v74[13] = v83;
        v74[14] = index$merge_db_STAR_.const__32;
        v84 = mid_avetid;
        mid_avetid = null;
        v74[15] = v84;
        v74[16] = index$merge_db_STAR_.const__41;
        v85 = aevtid;
        aevtid = null;
        v74[17] = v85;
        v74[18] = index$merge_db_STAR_.const__82;
        v74[19] = Numbers.unchecked_inc((Object)((Db)db).index_rev);
        v74[20] = index$merge_db_STAR_.const__62;
        v86 = raetid;
        raetid = null;
        v74[21] = v86;
        v74[22] = index$merge_db_STAR_.const__61;
        v87 = mid_raetid;
        mid_raetid = null;
        v74[23] = v87;
        v74[24] = index$merge_db_STAR_.const__84;
        v74[25] = ((IFn)index$merge_db_STAR_.const__48.getRawRoot()).invoke((Object)"datomic.buildRevision");
        v74[26] = index$merge_db_STAR_.const__34;
        v88 = hist_avetid;
        hist_avetid = null;
        v74[27] = v88;
        v74[28] = index$merge_db_STAR_.const__22;
        v89 = hist_eavtid;
        hist_eavtid = null;
        v74[29] = v89;
        v74[30] = index$merge_db_STAR_.const__63;
        v90 = hist_raetid;
        hist_raetid = null;
        v74[31] = v90;
        v74[32] = index$merge_db_STAR_.const__40;
        v91 = mid_aevtid;
        mid_aevtid = null;
        v74[33] = v91;
        v74[34] = index$merge_db_STAR_.const__85;
        v74[35] = index$merge_db_STAR_.const__66;
        v74[36] = index$merge_db_STAR_.const__33;
        v92 = avetid;
        avetid = null;
        v74[37] = v92;
        v74[38] = index$merge_db_STAR_.const__42;
        v93 = hist_aevtid;
        hist_aevtid = null;
        v74[39] = v93;
        v74[40] = index$merge_db_STAR_.const__19;
        v94 = mid_eavtid;
        mid_eavtid = null;
        v74[41] = v94;
        root = v71.invoke(v72.invoke(v73, (Object)RT.mapUniqueKeys((Object[])v74)), index$merge_db_STAR_.const__28.getRawRoot());
        v95 = cstore;
        cstore = null;
        v96 = new Object[2];
        v96[0] = rootid;
        v97 = root;
        root = null;
        v96[1] = v97;
        ((IFn)index$merge_db_STAR_.const__86.getRawRoot()).invoke(v95, (Object)RT.mapUniqueKeys((Object[])v96));
        v98 = segs_written_ref;
        segs_written_ref = null;
        written = Numbers.unchecked_inc((Object)((IFn)index$merge_db_STAR_.const__68.getRawRoot()).invoke(v98));
        v99 = dirs_written_ref;
        dirs_written_ref = null;
        dirs_written = ((IFn)index$merge_db_STAR_.const__68.getRawRoot()).invoke(v99);
        logger = LoggerFactory.getLogger((String)"datomic.index");
        if (logger.isInfoEnabled()) {
            v100 = logger;
            logger = null;
            v101 = (IFn)index$merge_db_STAR_.const__8.getRawRoot();
            v102 = new Object[14];
            v102[0] = index$merge_db_STAR_.const__9;
            v102[1] = index$merge_db_STAR_.const__87;
            v102[2] = index$merge_db_STAR_.const__88;
            v102[3] = rootid;
            v102[4] = index$merge_db_STAR_.const__89;
            v102[5] = written;
            v102[6] = index$merge_db_STAR_.const__90;
            v102[7] = dirs_written;
            v102[8] = index$merge_db_STAR_.const__91;
            v103 = as_of_t;
            as_of_t = null;
            v102[9] = v103;
            v102[10] = index$merge_db_STAR_.const__92;
            v102[11] = ((IFn)index$merge_db_STAR_.const__93.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.unchecked_minus((long)System.nanoTime(), (long)job_started)));
            v102[12] = index$merge_db_STAR_.const__95;
            v104 = index$merge_db_STAR_.__thunk__7__;
            v105 = index$merge_db_STAR_.__thunk__6__;
            v106 = db;
            db = null;
            v107 = v105.get(v106);
            if (v105 == v107) {
                index$merge_db_STAR_.__thunk__6__ = index$merge_db_STAR_.__site__6__.fault(v106);
                v107 = index$merge_db_STAR_.__thunk__6__.get(v106);
            }
            if (v104 == (v108 = v104.get(v107))) {
                index$merge_db_STAR_.__thunk__7__ = index$merge_db_STAR_.__site__7__.fault(v107);
                v108 = index$merge_db_STAR_.__thunk__7__.get(v107);
            }
            v109 = btset = v108;
            btset = null;
            v102[13] = Numbers.num((long)((IDataSet)v109).longCount());
            v100.info((String)v101.invoke((Object)RT.mapUniqueKeys((Object[])v102)));
        }
        v110 = written;
        written = null;
        ((IFn)index$merge_db_STAR_.const__97.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__98, (Object)v110);
        v111 = dirs_written;
        dirs_written = null;
        ((IFn)index$merge_db_STAR_.const__97.getRawRoot()).invoke((Object)index$merge_db_STAR_.const__99, v111);
        v112 = rootid;
        rootid = null;
        v113 = xpreds;
        xpreds = null;
        v114 = garbage;
        garbage = null;
        v115 = old_root_id;
        old_root_id = null;
        return Tuple.create((Object)v112, (Object)v113, (Object)((IFn)index$merge_db_STAR_.const__100.getRawRoot()).invoke(v114, ((IFn)index$merge_db_STAR_.const__74.getRawRoot()).invoke(v115)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        Object object9 = object;
        object = null;
        Object object10 = object2;
        object2 = null;
        Object object11 = object3;
        object3 = null;
        Object object12 = object4;
        object4 = null;
        Object object13 = object5;
        object5 = null;
        Object object14 = object6;
        object6 = null;
        Object object15 = object7;
        object7 = null;
        Object object16 = object8;
        object8 = null;
        return index$merge_db_STAR_.invokeStatic(object9, object10, object11, object12, object13, object14, object15, object16);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"atom");
        const__1 = 0L;
        const__2 = RT.var((String)"datomic.common", (String)"getx");
        const__3 = RT.var((String)"datomic.index", (String)"needs-new-avet");
        const__4 = RT.var((String)"datomic.index", (String)"add-avet-indexes");
        const__7 = RT.var((String)"datomic.index", (String)"excise-ents");
        const__8 = RT.var((String)"datomic.slf4j", (String)"process");
        const__9 = RT.keyword(null, (String)"event");
        const__10 = RT.keyword((String)"excise", (String)"ents");
        const__11 = RT.keyword(null, (String)"next-t");
        const__12 = RT.keyword(null, (String)"count");
        const__14 = RT.var((String)"datomic.excise", (String)"create-xpreds");
        const__17 = RT.keyword(null, (String)"eavt");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-db"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"eavt")});
        const__19 = RT.keyword(null, (String)"eavt-mid");
        const__20 = RT.var((String)"datomic.index", (String)"idx-key");
        const__21 = RT.keyword(null, (String)"eavt-main");
        const__22 = RT.keyword(null, (String)"eavt-hist");
        const__23 = RT.var((String)"clojure.core", (String)"constantly");
        const__24 = 42L;
        const__25 = RT.var((String)"datomic.index", (String)"dir-partition-size");
        const__26 = RT.var((String)"datomic.db", (String)"eavt-cmp");
        const__27 = RT.var((String)"datomic.index", (String)"eavt-cmpi");
        const__28 = RT.var((String)"datomic.index", (String)"common-write-handlers");
        const__30 = RT.keyword(null, (String)"avet");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-db"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"avet")});
        const__32 = RT.keyword(null, (String)"avet-mid");
        const__33 = RT.keyword(null, (String)"avet-main");
        const__34 = RT.keyword(null, (String)"avet-hist");
        const__35 = RT.var((String)"datomic.db", (String)"avet-cmp");
        const__36 = RT.var((String)"datomic.index", (String)"avet-cmpi");
        const__38 = RT.keyword(null, (String)"aevt");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-db"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"aevt")});
        const__40 = RT.keyword(null, (String)"aevt-mid");
        const__41 = RT.keyword(null, (String)"aevt-main");
        const__42 = RT.keyword(null, (String)"aevt-hist");
        const__43 = RT.var((String)"datomic.db", (String)"aevt-cmp");
        const__44 = RT.var((String)"datomic.index", (String)"aevt-cmpi");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-db"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"ft")});
        const__48 = RT.var((String)"datomic.config", (String)"property");
        const__49 = RT.var((String)"datomic.cluster", (String)"queueing-writer");
        const__50 = RT.var((String)"datomic.cluster", (String)"BOUNDING_TIMEOUT_MSEC");
        const__51 = RT.var((String)"datomic.fulltext", (String)"build-index");
        const__52 = RT.var((String)"clojure.core", (String)"map");
        const__53 = RT.var((String)"datomic.db", (String)"fulltext-attrs");
        const__54 = RT.keyword(null, (String)"fulltext");
        const__55 = RT.keyword(null, (String)"fulltext-hist");
        const__56 = RT.var((String)"datomic.common", (String)"bounded-deref");
        const__57 = RT.var((String)"datomic.cluster", (String)"finish-writer");
        const__59 = RT.keyword(null, (String)"raet");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-db"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"raet")});
        const__61 = RT.keyword(null, (String)"raet-mid");
        const__62 = RT.keyword(null, (String)"raet-main");
        const__63 = RT.keyword(null, (String)"raet-hist");
        const__64 = RT.var((String)"datomic.db", (String)"raet-cmp");
        const__65 = RT.var((String)"datomic.index", (String)"raet-cmpi");
        const__66 = 2L;
        const__68 = RT.var((String)"clojure.core", (String)"deref");
        const__69 = RT.var((String)"clojure.core", (String)"seq");
        const__70 = RT.var((String)"datomic.index", (String)"dropped-avet-aids");
        const__71 = RT.var((String)"datomic.index", (String)"drop-avet-indexes");
        const__72 = RT.var((String)"clojure.core", (String)"into");
        const__73 = RT.var((String)"clojure.core", (String)"concat");
        const__74 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__75 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__76 = RT.var((String)"datomic.index", (String)"fress");
        const__77 = RT.var((String)"clojure.core", (String)"merge");
        const__78 = RT.keyword(null, (String)"birth-level");
        const__79 = RT.keyword(null, (String)"nextT");
        const__80 = RT.keyword(null, (String)"schema-level");
        const__81 = RT.keyword(null, (String)"basisT");
        const__82 = RT.keyword(null, (String)"rev");
        const__84 = RT.keyword(null, (String)"buildRevision");
        const__85 = RT.keyword(null, (String)"version");
        const__86 = RT.var((String)"datomic.index", (String)"write-vals");
        const__87 = RT.keyword((String)"index", (String)"create-index");
        const__88 = RT.keyword(null, (String)"root-id");
        const__89 = RT.keyword(null, (String)"written");
        const__90 = RT.keyword(null, (String)"dirs-written");
        const__91 = RT.keyword(null, (String)"as-of-t");
        const__92 = RT.keyword(null, (String)"msec");
        const__93 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__95 = RT.keyword(null, (String)"datoms");
        const__97 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__98 = RT.keyword(null, (String)"IndexWrites");
        const__99 = RT.keyword(null, (String)"IndexDirWrites");
        const__100 = RT.var((String)"clojure.core", (String)"conj");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext-hist"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext-hist"));
        __thunk__3__ = __site__3__;
        __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"birth-level"));
        __thunk__4__ = __site__4__;
        __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"schema-level"));
        __thunk__5__ = __site__5__;
        __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"indexing"));
        __thunk__6__ = __site__6__;
        __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
        __thunk__7__ = __site__7__;
    }
}

