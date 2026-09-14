/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import datomic.cluster.ClusteredStore;
import datomic.process.CriticalFailure;

public final class index$merge_db$fn__15867$fn__15871
extends AFunction {
    Object cstore;
    Object storage_index_root_ref;
    Object olookup;
    Object db;
    Object index_ref_key;
    Object as_of_t;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Keyword const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    public static final Keyword const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public index$merge_db$fn__15867$fn__15871(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.cstore = object;
        this.storage_index_root_ref = object2;
        this.olookup = object3;
        this.db = object4;
        this.index_ref_key = object5;
        this.as_of_t = object6;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            block10: {
                block9: {
                    v0 = (IFn)index$merge_db$fn__15867$fn__15871.const__0.getRawRoot();
                    v1 = index$merge_db$fn__15867$fn__15871.__thunk__0__;
                    v2 = this.storage_index_root_ref;
                    v3 = v1.get(v2);
                    if (v1 == v3) {
                        index$merge_db$fn__15867$fn__15871.__thunk__0__ = index$merge_db$fn__15867$fn__15871.__site__0__.fault(v2);
                        v3 = index$merge_db$fn__15867$fn__15871.__thunk__0__.get(v2);
                    }
                    vec__15872 = v0.invoke(this.cstore, this.olookup, this.db, this.as_of_t, v3, null, (Object)Boolean.TRUE, (Object)Boolean.TRUE);
                    rootid = RT.nth((Object)vec__15872, (int)RT.uncheckedIntCast((long)0L), null);
                    xpreds = RT.nth((Object)vec__15872, (int)RT.uncheckedIntCast((long)1L), null);
                    v4 = vec__15872;
                    vec__15872 = null;
                    garbage = RT.nth((Object)v4, (int)RT.uncheckedIntCast((long)2L), null);
                    cluster_rev = Numbers.unchecked_inc((Object)((IFn)index$merge_db$fn__15867$fn__15871.const__7.getRawRoot()).invoke(this.storage_index_root_ref, (Object)index$merge_db$fn__15867$fn__15871.const__8));
                    ((IFn)index$merge_db$fn__15867$fn__15871.const__9.getRawRoot()).invoke();
                    v5 = (IFn)index$merge_db$fn__15867$fn__15871.const__12.getRawRoot();
                    v6 = this.cstore;
                    if (Util.classOf((Object)v6) == index$merge_db$fn__15867$fn__15871.__cached_class__0) ** GOTO lbl25
                    if (!(v6 instanceof ClusteredStore)) {
                        v6 = v6;
                        index$merge_db$fn__15867$fn__15871.__cached_class__0 = Util.classOf((Object)v6);
lbl25:
                        // 2 sources

                        v7 = index$merge_db$fn__15867$fn__15871.const__13.getRawRoot().invoke(v6, this.index_ref_key, (Object)cluster_rev, ((IFn)index$merge_db$fn__15867$fn__15871.const__14.getRawRoot()).invoke(rootid));
                    } else {
                        v7 = ((ClusteredStore)v6).set_ref(this.index_ref_key, cluster_rev, ((IFn)index$merge_db$fn__15867$fn__15871.const__14.getRawRoot()).invoke(rootid));
                    }
                    if (!Util.equiv((Object)index$merge_db$fn__15867$fn__15871.const__11, (Object)v5.invoke(v7))) break block9;
                    v8 = rootid;
                    rootid = null;
                    ret = ((IFn)index$merge_db$fn__15867$fn__15871.const__15.getRawRoot()).invoke(this.olookup, v8);
                    v9 = new Object[6];
                    v9[0] = index$merge_db$fn__15867$fn__15871.const__1;
                    v9[1] = index$merge_db$fn__15867$fn__15871.const__17;
                    v9[2] = index$merge_db$fn__15867$fn__15871.const__18;
                    v9[3] = this.cstore;
                    v9[4] = index$merge_db$fn__15867$fn__15871.const__19;
                    v10 = garbage;
                    garbage = null;
                    v9[5] = v10;
                    ((IFn)index$merge_db$fn__15867$fn__15871.const__16.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v9));
                    v11 = new Object[4];
                    v11[0] = index$merge_db$fn__15867$fn__15871.const__20;
                    v12 = ret;
                    ret = null;
                    v11[1] = v12;
                    v11[2] = index$merge_db$fn__15867$fn__15871.const__21;
                    v13 = xpreds;
                    xpreds = null;
                    v11[3] = v13;
                    v14 = RT.mapUniqueKeys((Object[])v11);
                    break block10;
                }
                v15 = index$merge_db$fn__15867$fn__15871.const__23.getRawRoot();
                if (Util.classOf((Object)v15) == index$merge_db$fn__15867$fn__15871.__cached_class__1) ** GOTO lbl60
                if (!(v15 instanceof CriticalFailure)) {
                    v15 = v15;
                    index$merge_db$fn__15867$fn__15871.__cached_class__1 = Util.classOf((Object)v15);
lbl60:
                    // 2 sources

                    v16 = cluster_rev;
                    cluster_rev = null;
                    v14 = index$merge_db$fn__15867$fn__15871.const__22.getRawRoot().invoke(v15, ((IFn)index$merge_db$fn__15867$fn__15871.const__24.getRawRoot()).invoke((Object)"Conflict updating index root at rev ", (Object)v16));
                } else {
                    v17 = cluster_rev;
                    cluster_rev = null;
                    v14 = ((CriticalFailure)v15).fail(((IFn)index$merge_db$fn__15867$fn__15871.const__24.getRawRoot()).invoke((Object)"Conflict updating index root at rev ", (Object)v17));
                }
            }
            var7_7 = v14;
        }
        finally {
            ((IFn)index$merge_db$fn__15867$fn__15871.const__25.getRawRoot()).invoke();
        }
        return var7_7;
    }

    static {
        const__0 = RT.var((String)"datomic.index", (String)"merge-db*");
        const__1 = RT.keyword(null, (String)"key");
        const__7 = RT.var((String)"datomic.common", (String)"getx");
        const__8 = RT.keyword(null, (String)"rev");
        const__9 = RT.var((String)"datomic.process", (String)"throw-if-failing!");
        const__11 = RT.keyword(null, (String)"ok");
        const__12 = RT.var((String)"clojure.core", (String)"deref");
        const__13 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__14 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__15 = RT.var((String)"datomic.index", (String)"load-index");
        const__16 = RT.var((String)"datomic.process.events", (String)"publish");
        const__17 = RT.keyword((String)"datomic.garbage", (String)"mark");
        const__18 = RT.keyword(null, (String)"cluster");
        const__19 = RT.keyword(null, (String)"garbage");
        const__20 = RT.keyword(null, (String)"new-index");
        const__21 = RT.keyword(null, (String)"xpreds");
        const__22 = RT.var((String)"datomic.process", (String)"fail");
        const__23 = RT.var((String)"datomic.process", (String)"instance");
        const__24 = RT.var((String)"clojure.core", (String)"str");
        const__25 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

