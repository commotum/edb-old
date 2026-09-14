/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.peer.TWatcher;
import org.slf4j.LoggerFactory;

public final class Connection$fn__21555
extends AFunction {
    Object tx_watcher;
    Object db_id;
    Object idx_watcher;
    Object bg_watcher;
    Object db_ref;
    Object olookup;
    Object cluster;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    public static final Keyword const__17;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Keyword const__24;
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

    public Connection$fn__21555(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.tx_watcher = object;
        this.db_id = object2;
        this.idx_watcher = object3;
        this.bg_watcher = object4;
        this.db_ref = object5;
        this.olookup = object6;
        this.cluster = object7;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            block20: {
                block19: {
                    v0 = idxroot = ((IFn)Connection$fn__21555.const__0.getRawRoot()).invoke(this.cluster);
                    idxroot = null;
                    v1 = idx = ((IFn)Connection$fn__21555.const__1.getRawRoot()).invoke(this.olookup, v0);
                    idx = null;
                    adopt_db = ((IFn)Connection$fn__21555.const__2.getRawRoot()).invoke(this.db_id, v1);
                    v2 = temp__5455__auto__21558 = ((IFn)Connection$fn__21555.const__3.getRawRoot()).invoke(this.db_ref, adopt_db);
                    if (v2 == null || v2 == Boolean.FALSE) break block19;
                    v3 = temp__5455__auto__21558;
                    temp__5455__auto__21558 = null;
                    map__21556 = v3;
                    v4 = ((IFn)Connection$fn__21555.const__4.getRawRoot()).invoke(map__21556);
                    if (v4 != null && v4 != Boolean.FALSE) {
                        v5 = map__21556;
                        map__21556 = null;
                        v6 = PersistentHashMap.create((ISeq)((ISeq)((IFn)Connection$fn__21555.const__5.getRawRoot()).invoke(v5)));
                    } else {
                        v6 = map__21556;
                        map__21556 = null;
                    }
                    map__21556 = v6;
                    basis_db = RT.get((Object)map__21556, (Object)Connection$fn__21555.const__7);
                    adopted_db = RT.get((Object)map__21556, (Object)Connection$fn__21555.const__8);
                    msec = RT.get((Object)map__21556, (Object)Connection$fn__21555.const__9);
                    v7 = map__21556;
                    map__21556 = null;
                    iterations = RT.get((Object)v7, (Object)Connection$fn__21555.const__10);
                    v8 = this.tx_watcher;
                    if (Util.classOf((Object)v8) == Connection$fn__21555.__cached_class__0) ** GOTO lbl32
                    if (!(v8 instanceof TWatcher)) {
                        v8 = v8;
                        Connection$fn__21555.__cached_class__0 = Util.classOf((Object)v8);
lbl32:
                        // 2 sources

                        v9 = Connection$fn__21555.const__11.getRawRoot().invoke(v8, adopted_db);
                    } else {
                        v9 = ((TWatcher)v8).release_pending_syncs(adopted_db);
                    }
                    if (Util.classOf((Object)(v10 = this.idx_watcher)) == Connection$fn__21555.__cached_class__1) ** GOTO lbl39
                    if (!(v10 instanceof TWatcher)) {
                        v10 = v10;
                        Connection$fn__21555.__cached_class__1 = Util.classOf((Object)v10);
lbl39:
                        // 2 sources

                        v11 = Connection$fn__21555.const__11.getRawRoot().invoke(v10, adopted_db);
                    } else {
                        v11 = ((TWatcher)v10).release_pending_syncs(adopted_db);
                    }
                    if (Util.classOf((Object)(v12 = this.bg_watcher)) == Connection$fn__21555.__cached_class__2) ** GOTO lbl46
                    if (!(v12 instanceof TWatcher)) {
                        v12 = v12;
                        Connection$fn__21555.__cached_class__2 = Util.classOf((Object)v12);
lbl46:
                        // 2 sources

                        v13 = Connection$fn__21555.const__11.getRawRoot().invoke(v12, adopted_db);
                    } else {
                        v13 = ((TWatcher)v12).release_pending_syncs(adopted_db);
                    }
                    logger = LoggerFactory.getLogger((String)"datomic.peer");
                    if (logger.isInfoEnabled()) {
                        v14 = logger;
                        logger = null;
                        v15 = (IFn)Connection$fn__21555.const__12.getRawRoot();
                        v16 = new Object[14];
                        v16[0] = Connection$fn__21555.const__13;
                        v16[1] = Connection$fn__21555.const__14;
                        v16[2] = Connection$fn__21555.const__15;
                        v17 = Connection$fn__21555.__thunk__0__;
                        v18 = basis_db;
                        v19 = v17.get(v18);
                        if (v17 == v19) {
                            Connection$fn__21555.__thunk__0__ = Connection$fn__21555.__site__0__.fault(v18);
                            v19 = Connection$fn__21555.__thunk__0__.get(v18);
                        }
                        v16[3] = v19;
                        v16[4] = Connection$fn__21555.const__17;
                        v20 = Connection$fn__21555.__thunk__1__;
                        v21 = basis_db;
                        basis_db = null;
                        v22 = v20.get(v21);
                        if (v20 == v22) {
                            Connection$fn__21555.__thunk__1__ = Connection$fn__21555.__site__1__.fault(v21);
                            v22 = Connection$fn__21555.__thunk__1__.get(v21);
                        }
                        v16[5] = v22;
                        v16[6] = Connection$fn__21555.const__19;
                        v23 = Connection$fn__21555.__thunk__2__;
                        v24 = adopted_db;
                        v25 = v23.get(v24);
                        if (v23 == v25) {
                            Connection$fn__21555.__thunk__2__ = Connection$fn__21555.__site__2__.fault(v24);
                            v25 = Connection$fn__21555.__thunk__2__.get(v24);
                        }
                        v16[7] = v25;
                        v16[8] = Connection$fn__21555.const__20;
                        v26 = Connection$fn__21555.__thunk__3__;
                        v27 = adopted_db;
                        adopted_db = null;
                        v28 = v26.get(v27);
                        if (v26 == v28) {
                            Connection$fn__21555.__thunk__3__ = Connection$fn__21555.__site__3__.fault(v27);
                            v28 = Connection$fn__21555.__thunk__3__.get(v27);
                        }
                        v16[9] = v28;
                        v16[10] = Connection$fn__21555.const__9;
                        v29 = msec;
                        msec = null;
                        v16[11] = v29;
                        v16[12] = Connection$fn__21555.const__10;
                        v30 = iterations;
                        iterations = null;
                        v16[13] = v30;
                        v14.info((String)v15.invoke((Object)RT.mapUniqueKeys((Object[])v16)));
                    }
                    v31 = null;
                    break block20;
                }
                logger = LoggerFactory.getLogger((String)"datomic.peer");
                if (logger.isWarnEnabled()) {
                    v32 = logger;
                    logger = null;
                    v33 = (IFn)Connection$fn__21555.const__12.getRawRoot();
                    v34 = new Object[6];
                    v34[0] = Connection$fn__21555.const__13;
                    v34[1] = Connection$fn__21555.const__21;
                    v34[2] = Connection$fn__21555.const__19;
                    v35 = Connection$fn__21555.__thunk__4__;
                    v36 = adopt_db;
                    v37 = v35.get(v36);
                    if (v35 == v37) {
                        Connection$fn__21555.__thunk__4__ = Connection$fn__21555.__site__4__.fault(v36);
                        v37 = Connection$fn__21555.__thunk__4__.get(v36);
                    }
                    v34[3] = v37;
                    v34[4] = Connection$fn__21555.const__20;
                    v38 = Connection$fn__21555.__thunk__5__;
                    v39 = adopt_db;
                    adopt_db = null;
                    v40 = v38.get(v39);
                    if (v38 == v40) {
                        Connection$fn__21555.__thunk__5__ = Connection$fn__21555.__site__5__.fault(v39);
                        v40 = Connection$fn__21555.__thunk__5__.get(v39);
                    }
                    v34[5] = v40;
                    v32.warn((String)v33.invoke((Object)RT.mapUniqueKeys((Object[])v34)));
                }
                v31 = null;
            }
            var12_16 = v31;
        }
        catch (Throwable t__9147__auto__) {
            logger = LoggerFactory.getLogger((String)"datomic.peer");
            ex = t__9147__auto__;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)Connection$fn__21555.const__12.getRawRoot()).invoke((Object)"error executing future"), ex);
                v41 = logger;
                logger = null;
                v42 = ex;
                ex = null;
                ((IFn)Connection$fn__21555.const__22.getRawRoot()).invoke((Object)v41, (Object)v42);
            }
            ((IFn)Connection$fn__21555.const__23.getRawRoot()).invoke((Object)Connection$fn__21555.const__24);
            t__9147__auto__ = null;
            throw t__9147__auto__;
        }
        return var12_16;
    }

    static {
        const__0 = RT.var((String)"datomic.index", (String)"find-index-root-id");
        const__1 = RT.var((String)"datomic.index", (String)"load-index");
        const__2 = RT.var((String)"datomic.db", (String)"db");
        const__3 = RT.var((String)"datomic.adopter", (String)"adopt-index");
        const__4 = RT.var((String)"clojure.core", (String)"seq?");
        const__5 = RT.var((String)"clojure.core", (String)"seq");
        const__7 = RT.keyword(null, (String)"basis-db");
        const__8 = RT.keyword(null, (String)"adopted-db");
        const__9 = RT.keyword(null, (String)"msec");
        const__10 = RT.keyword(null, (String)"iterations");
        const__11 = RT.var((String)"datomic.peer", (String)"release-pending-syncs");
        const__12 = RT.var((String)"datomic.slf4j", (String)"process");
        const__13 = RT.keyword(null, (String)"event");
        const__14 = RT.keyword((String)"peer", (String)"adopt-completed");
        const__15 = RT.keyword(null, (String)"previous-root-id");
        const__17 = RT.keyword(null, (String)"previous-t");
        const__19 = RT.keyword(null, (String)"root-id");
        const__20 = RT.keyword(null, (String)"t");
        const__21 = RT.keyword((String)"peer", (String)"adopt-failed");
        const__22 = RT.var((String)"datomic.slf4j", (String)"caused-by");
        const__23 = RT.var((String)"datomic.monitor", (String)"alarm");
        const__24 = RT.keyword(null, (String)"UnhandledException");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"index-root-id"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"index-root-id"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
        __thunk__3__ = __site__3__;
        __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"index-root-id"));
        __thunk__4__ = __site__4__;
        __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
        __thunk__5__ = __site__5__;
    }
}

