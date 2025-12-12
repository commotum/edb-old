/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.PersistentQueue
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
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentQueue;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.AsyncWriter;
import datomic.index$merge_one_index$drainq__15468;
import datomic.index$merge_one_index$excise_QMARK___15458;
import datomic.index$merge_one_index$f__15304__auto____15473;
import datomic.index$merge_one_index$fn__15452;
import datomic.index$merge_one_index$fn__15454;
import datomic.index$merge_one_index$fn__15462;
import datomic.index$merge_one_index$fn__15507;
import datomic.index$merge_one_index$mkdes__15464;
import datomic.index$merge_one_index$mkroot__15509;
import datomic.index.RootNode;
import org.slf4j.LoggerFactory;

public final class index$merge_one_index
extends AFunction {
    private static Class __cached_class__0;
    public static final AFn const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Object const__13;
    public static final Keyword const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Keyword const__24;
    public static final Keyword const__25;
    public static final Keyword const__26;
    public static final Var const__28;
    public static final Keyword const__29;
    public static final Var const__30;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final Var const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Var const__38;
    public static final Var const__39;
    public static final Var const__40;
    public static final Var const__41;
    public static final Var const__42;
    public static final Var const__43;
    public static final Var const__44;
    public static final Var const__45;
    public static final Var const__46;
    public static final Var const__47;
    public static final Var const__48;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object db, Object cstore, Object olookup, Object old_root_id, Object data, Object garbage_ids, Object partfn, Object cmp, Object write_handlers, Object filter_retractions_QMARK_, Object as_of_t, Object idx, Object xpreds, Object filter_segids, Object xcmp, Object segs_written_ref, Object dirs_written_ref) {
        try {
            block14: {
                v0 = pario = ((IFn)index$merge_one_index.const__1.getRawRoot()).invoke((Object)"datomic.indexIOParallelism");
                if (v0 != null && v0 != Boolean.FALSE) {
                    v1 = cstore;
                    cstore = null;
                    v2 = ((IFn)index$merge_one_index.const__2.getRawRoot()).invoke(v1, pario, index$merge_one_index.const__3.getRawRoot(), (Object)new index$merge_one_index$fn__15452());
                } else {
                    v2 = cstore;
                    cstore = null;
                }
                cstore = v2;
                lt = ((IFn)index$merge_one_index.const__4.getRawRoot()).invoke(cmp);
                v3 = old_root_id;
                old_root = v3 != null && v3 != Boolean.FALSE ? ((IFn)index$merge_one_index.const__5.getRawRoot()).invoke(olookup, old_root_id) : null;
                vec__15446 = ((IFn)index$merge_one_index.const__6.getRawRoot()).invoke((Object)index$merge_one_index.const__7, cstore, (Object)index$merge_one_index.const__8, olookup, (Object)index$merge_one_index.const__9, idx, (Object)index$merge_one_index.const__10, ((IFn)index$merge_one_index.const__11.getRawRoot()).invoke(idx), (Object)index$merge_one_index.const__12, index$merge_one_index.const__13, (Object)index$merge_one_index.const__14, ((IFn)index$merge_one_index.const__15.getRawRoot()).invoke(index$merge_one_index.const__16.getRawRoot()));
                es = RT.nth((Object)vec__15446, (int)RT.uncheckedIntCast((long)0L), null);
                des_ch = RT.nth((Object)vec__15446, (int)RT.uncheckedIntCast((long)1L), null);
                v4 = vec__15446;
                vec__15446 = null;
                rootnode_fut = RT.nth((Object)v4, (int)RT.uncheckedIntCast((long)2L), null);
                v5 = filter_retractions_QMARK_;
                filter_retractions_QMARK_ = null;
                retractions = v5 != null && v5 != Boolean.FALSE ? PersistentVector.EMPTY : null;
                v6 = old_root_id;
                if (v6 != null && v6 != Boolean.FALSE) {
                    v7 = xcmp;
                    xcmp = null;
                    v8 = index = ((IFn)index$merge_one_index.const__21.getRawRoot()).invoke(olookup, v7, old_root_id);
                    index = null;
                    v9 = ((IFn)index$merge_one_index.const__22.getRawRoot()).invoke((Object)new index$merge_one_index$fn__15454(v8), (Object)PersistentHashSet.EMPTY, xpreds);
                } else {
                    v9 = null;
                }
                xsegs = v9;
                logger = LoggerFactory.getLogger((String)"datomic.index");
                if (logger.isInfoEnabled()) {
                    v10 = logger;
                    logger = null;
                    v11 = new Object[6];
                    v11[0] = index$merge_one_index.const__24;
                    v11[1] = index$merge_one_index.const__25;
                    v11[2] = index$merge_one_index.const__26;
                    v11[3] = RT.count((Object)xsegs);
                    v11[4] = index$merge_one_index.const__9;
                    v12 = idx;
                    idx = null;
                    v11[5] = v12;
                    v10.info((String)((IFn)index$merge_one_index.const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v11)));
                }
                ((IFn)index$merge_one_index.const__28.getRawRoot()).invoke((Object)index$merge_one_index.const__29, (Object)RT.count(xsegs));
                v13 = xpreds;
                xpreds = null;
                excise_QMARK_ = new index$merge_one_index$excise_QMARK___15458(v13);
                v14 = garbage_ids;
                garbage_ids = null;
                garbage_ids = ((IFn)index$merge_one_index.const__30.getRawRoot()).invoke(v14, xsegs);
                v15 = old_root;
                dirs = v15 != null && v15 != Boolean.FALSE ? ((IFn)index$merge_one_index.const__31.getRawRoot()).invoke((Object)new index$merge_one_index$fn__15462(olookup), ((RootNode)old_root).dirids) : null;
                v16 = xsegs;
                xsegs = null;
                v17 = filter_segids;
                filter_segids = null;
                mkdes = new index$merge_one_index$mkdes__15464(pario, cstore, v16, olookup, (Object)excise_QMARK_, write_handlers, segs_written_ref, v17);
                drainq = new index$merge_one_index$drainq__15468();
                v18 = cmp;
                cmp = null;
                v19 = drainq;
                drainq = null;
                v20 = partfn;
                partfn = null;
                v21 = db;
                db = null;
                v22 = lt;
                lt = null;
                v23 = f__15304__auto__15512 = new index$merge_one_index$f__15304__auto____15473(v18, (Object)v19, cstore, olookup, v20, write_handlers, segs_written_ref, v21, v22);
                f__15304__auto__15512 = null;
                v24 = es;
                es = null;
                v25 = garbage_ids;
                garbage_ids = null;
                v26 = retractions;
                retractions = null;
                v27 = mkdes;
                mkdes = null;
                v28 = dirs;
                dirs = null;
                v29 = as_of_t;
                as_of_t = null;
                v30 = excise_QMARK_;
                excise_QMARK_ = null;
                v31 = data;
                data = null;
                vec__15449 = ((IFn)v23).invoke(v24, v25, (Object)v26, ((IFn)index$merge_one_index.const__32.getRawRoot()).invoke(((IFn)index$merge_one_index.const__33.getRawRoot()).invoke(index$merge_one_index.const__34.getRawRoot(), ((IFn)index$merge_one_index.const__35.getRawRoot()).invoke((Object)v27, v28))), ((IFn)index$merge_one_index.const__32.getRawRoot()).invoke(((IFn)index$merge_one_index.const__36.getRawRoot()).invoke((Object)new index$merge_one_index$fn__15507(v29), ((IFn)index$merge_one_index.const__37.getRawRoot()).invoke((Object)v30, v31))), (Object)PersistentQueue.EMPTY);
                RT.nth((Object)vec__15449, (int)RT.uncheckedIntCast((long)0L), null);
                garbage = RT.nth((Object)vec__15449, (int)RT.uncheckedIntCast((long)1L), null);
                v32 = vec__15449;
                vec__15449 = null;
                retractions = RT.nth((Object)v32, (int)RT.uncheckedIntCast((long)2L), null);
                v33 = des_ch;
                des_ch = null;
                ((IFn)index$merge_one_index.const__38.getRawRoot()).invoke(v33);
                v34 = rootnode_fut;
                rootnode_fut = null;
                rootnode = ((IFn)index$merge_one_index.const__15.getRawRoot()).invoke(v34);
                v35 = dirs_written_ref;
                dirs_written_ref = null;
                ((IFn)index$merge_one_index.const__39.getRawRoot()).invoke(v35, index$merge_one_index.const__40.getRawRoot(), (Object)RT.count((Object)rootnode));
                mkroot = new index$merge_one_index$mkroot__15509();
                rootid = ((IFn)index$merge_one_index.const__41.getRawRoot()).invoke();
                v36 = mkroot;
                mkroot = null;
                v37 = rootnode;
                rootnode = null;
                nroot = ((IFn)v36).invoke(v37);
                v38 = new Object[2];
                v38[0] = rootid;
                v39 = write_handlers;
                write_handlers = null;
                v38[1] = ((IFn)index$merge_one_index.const__42.getRawRoot()).invoke(nroot, v39);
                vmap = RT.mapUniqueKeys((Object[])v38);
                v40 = olookup;
                olookup = null;
                v41 = nroot;
                nroot = null;
                ((IFn)index$merge_one_index.const__43.getRawRoot()).invoke(v40, rootid, v41);
                ((IFn)index$merge_one_index.const__44.getRawRoot()).invoke(cstore, (Object)vmap);
                v42 = pario;
                pario = null;
                if (v42 == null || v42 == Boolean.FALSE) break block14;
                v43 = (IFn)index$merge_one_index.const__45.getRawRoot();
                v44 = cstore;
                cstore = null;
                v45 = v44;
                if (Util.classOf((Object)v44) == index$merge_one_index.__cached_class__0) ** GOTO lbl143
                if (!(v45 instanceof AsyncWriter)) {
                    v45 = v45;
                    index$merge_one_index.__cached_class__0 = Util.classOf((Object)v45);
lbl143:
                    // 2 sources

                    v46 = index$merge_one_index.const__46.getRawRoot().invoke(v45);
                } else {
                    v46 = ((AsyncWriter)v45).finish_writer();
                }
                v43.invoke(v46, index$merge_one_index.const__3.getRawRoot());
            }
            v47 = segs_written_ref;
            segs_written_ref = null;
            v48 = vmap;
            vmap = null;
            ((IFn)index$merge_one_index.const__39.getRawRoot()).invoke(v47, index$merge_one_index.const__40.getRawRoot(), (Object)RT.count((Object)v48));
            v49 = rootid;
            rootid = null;
            v50 = (IFn)index$merge_one_index.const__34.getRawRoot();
            v51 = garbage;
            garbage = null;
            v52 = old_root;
            if (v52 != null && v52 != Boolean.FALSE) {
                v53 = old_root;
                old_root = null;
                v54 = ((IFn)index$merge_one_index.const__31.getRawRoot()).invoke(index$merge_one_index.const__47.getRawRoot(), ((RootNode)v53).dirids);
            } else {
                v54 = null;
            }
            v55 = old_root_id;
            if (v55 != null && v55 != Boolean.FALSE) {
                v56 = old_root_id;
                old_root_id = null;
                v57 = Tuple.create((Object)((IFn)index$merge_one_index.const__47.getRawRoot()).invoke(v56));
            } else {
                v57 = null;
            }
            v58 = retractions;
            retractions = null;
            var45_45 = Tuple.create((Object)v49, (Object)v50.invoke(v51, v54, v57), (Object)v58);
        }
        catch (Throwable ex) {
            logger = LoggerFactory.getLogger((String)"datomic.index");
            ex = ex;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)index$merge_one_index.const__23.getRawRoot()).invoke((Object)"merge-one-index failed"), ex);
                v59 = logger;
                logger = null;
                v60 = ex;
                ex = null;
                ((IFn)index$merge_one_index.const__48.getRawRoot()).invoke((Object)v59, (Object)v60);
            }
            ex = null;
            throw ex;
        }
        return var45_45;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15, Object object16, Object object17) {
        Object object18 = object;
        object = null;
        Object object19 = object2;
        object2 = null;
        Object object20 = object3;
        object3 = null;
        Object object21 = object4;
        object4 = null;
        Object object22 = object5;
        object5 = null;
        Object object23 = object6;
        object6 = null;
        Object object24 = object7;
        object7 = null;
        Object object25 = object8;
        object8 = null;
        Object object26 = object9;
        object9 = null;
        Object object27 = object10;
        object10 = null;
        Object object28 = object11;
        object11 = null;
        Object object29 = object12;
        object12 = null;
        Object object30 = object13;
        object13 = null;
        Object object31 = object14;
        object14 = null;
        Object object32 = object15;
        object15 = null;
        Object object33 = object16;
        object16 = null;
        Object object34 = object17;
        object17 = null;
        return index$merge_one_index.invokeStatic(object18, object19, object20, object21, object22, object23, object24, object25, object26, object27, object28, object29, object30, object31, object32, object33, object34);
    }

    static {
        const__0 = (AFn)Symbol.intern(null, (String)"merge-one-index");
        const__1 = RT.var((String)"datomic.config", (String)"property");
        const__2 = RT.var((String)"datomic.cluster", (String)"queueing-writer");
        const__3 = RT.var((String)"datomic.cluster", (String)"BOUNDING_TIMEOUT_MSEC");
        const__4 = RT.var((String)"datomic.index", (String)"make-sparse-lt");
        const__5 = RT.var((String)"datomic.common", (String)"getx");
        const__6 = RT.var((String)"datomic.index", (String)"start-dirs-pipeline");
        const__7 = RT.keyword(null, (String)"cstore");
        const__8 = RT.keyword(null, (String)"olookup");
        const__9 = RT.keyword(null, (String)"idx");
        const__10 = RT.keyword(null, (String)"dirnode-size");
        const__11 = RT.var((String)"datomic.index", (String)"dir-partition-size");
        const__12 = RT.keyword(null, (String)"dirs-ahead");
        const__13 = 32L;
        const__14 = RT.keyword(null, (String)"serialize-par");
        const__15 = RT.var((String)"clojure.core", (String)"deref");
        const__16 = RT.var((String)"datomic.index", (String)"index-parallelism");
        const__21 = RT.var((String)"datomic.index", (String)"lookup-index");
        const__22 = RT.var((String)"clojure.core", (String)"reduce");
        const__23 = RT.var((String)"datomic.slf4j", (String)"process");
        const__24 = RT.keyword(null, (String)"event");
        const__25 = RT.keyword((String)"index", (String)"xsegs");
        const__26 = RT.keyword(null, (String)"count");
        const__28 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__29 = RT.keyword(null, (String)"ExciseSegments");
        const__30 = RT.var((String)"clojure.core", (String)"into");
        const__31 = RT.var((String)"clojure.core", (String)"map");
        const__32 = RT.var((String)"clojure.core", (String)"seq");
        const__33 = RT.var((String)"clojure.core", (String)"apply");
        const__34 = RT.var((String)"clojure.core", (String)"concat");
        const__35 = RT.var((String)"clojure.core", (String)"pmap");
        const__36 = RT.var((String)"clojure.core", (String)"filter");
        const__37 = RT.var((String)"clojure.core", (String)"remove");
        const__38 = RT.var((String)"clojure.core.async", (String)"close!");
        const__39 = RT.var((String)"clojure.core", (String)"swap!");
        const__40 = RT.var((String)"clojure.core", (String)"+");
        const__41 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__42 = RT.var((String)"datomic.index", (String)"fress");
        const__43 = RT.var((String)"datomic.cache", (String)"put");
        const__44 = RT.var((String)"datomic.index", (String)"write-vals");
        const__45 = RT.var((String)"datomic.common", (String)"bounded-deref");
        const__46 = RT.var((String)"datomic.cluster", (String)"finish-writer");
        const__47 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__48 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    }
}

