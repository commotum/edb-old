/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset.IDataSet;
import datomic.index.ITreeIter;

public final class index$disjoined_datoms
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static Object invokeStatic(Object db, Object tier, Object sort) {
        block16: {
            block17: {
                block19: {
                    block18: {
                        v0 = db;
                        db = null;
                        G__15688 = v0;
                        if (Util.identical((Object)G__15688, null)) {
                            v1 = null;
                        } else {
                            G__15688 = null;
                            v1 = G__15688 = ((IFn)tier).invoke(G__15688);
                        }
                        if (Util.identical(G__15688, null)) {
                            v2 = null;
                        } else {
                            v3 = sort;
                            sort = null;
                            v4 = G__15688;
                            G__15688 = null;
                            v2 = ((IFn)v3).invoke(v4);
                        }
                        v5 = temp__5457__auto__15701 = v2;
                        if (v5 == null || v5 == Boolean.FALSE) break block17;
                        v6 = temp__5457__auto__15701;
                        temp__5457__auto__15701 = null;
                        v7 = idx = v6;
                        idx = null;
                        G__15696 = v7;
                        if (Util.identical((Object)G__15696, null)) {
                            v8 = null;
                        } else {
                            G__15696 = null;
                            v8 = G__15696 = ((IDataSet)G__15696).seek();
                        }
                        if (!Util.identical((Object)G__15696, null)) break block18;
                        v9 = null;
                        break block19;
                    }
                    v10 = G__15696;
                    G__15696 = null;
                    v11 = v10;
                    if (Util.classOf((Object)v10) == index$disjoined_datoms.__cached_class__0) ** GOTO lbl40
                    if (!(v11 instanceof ITreeIter)) {
                        v11 = v11;
                        index$disjoined_datoms.__cached_class__0 = Util.classOf((Object)v11);
lbl40:
                        // 2 sources

                        v9 = index$disjoined_datoms.const__1.getRawRoot().invoke((Object)v11);
                    } else {
                        v9 = ((ITreeIter)v11).seg_PLUS_item_seq();
                    }
                }
                v12 = vec__15693 = (G__15692 = v9);
                vec__15693 = null;
                seq__15694 = ((IFn)index$disjoined_datoms.const__2.getRawRoot()).invoke(v12);
                first__15695 = ((IFn)index$disjoined_datoms.const__3.getRawRoot()).invoke(seq__15694);
                v13 = seq__15694;
                seq__15694 = null;
                seq__15694 = ((IFn)index$disjoined_datoms.const__4.getRawRoot()).invoke(v13);
                first__15695 = null;
                seq__15694 = null;
                result = Util.equiv((Object)tier, (Object)index$disjoined_datoms.const__6) != false ? RT.mapUniqueKeys((Object[])new Object[]{index$disjoined_datoms.const__7, PersistentVector.EMPTY, index$disjoined_datoms.const__8, PersistentVector.EMPTY, index$disjoined_datoms.const__9, PersistentVector.EMPTY}) : RT.mapUniqueKeys((Object[])new Object[]{index$disjoined_datoms.const__7, PersistentVector.EMPTY, index$disjoined_datoms.const__8, PersistentVector.EMPTY});
                v14 = G__15692;
                G__15692 = null;
                G__15692 = v14;
                v15 = result;
                result = null;
                result = v15;
                while (true) {
                    v16 = G__15692;
                    G__15692 = null;
                    v17 = vec__15697 = v16;
                    vec__15697 = null;
                    seq__15698 = ((IFn)index$disjoined_datoms.const__2.getRawRoot()).invoke(v17);
                    first__15699 = ((IFn)index$disjoined_datoms.const__3.getRawRoot()).invoke(seq__15698);
                    v18 = seq__15698;
                    seq__15698 = null;
                    seq__15698 = ((IFn)index$disjoined_datoms.const__4.getRawRoot()).invoke(v18);
                    v19 = first__15699;
                    first__15699 = null;
                    sd1 = v19;
                    v20 = seq__15698;
                    seq__15698 = null;
                    more = v20;
                    v21 = result;
                    result = null;
                    result = v21;
                    if (Util.identical((Object)sd1, null)) {
                        v22 = result;
                        result = null;
                        break block16;
                    }
                    v23 = index$disjoined_datoms.__thunk__1__;
                    v24 = index$disjoined_datoms.__thunk__0__;
                    v25 = sd1;
                    v26 = v24.get(v25);
                    if (v24 == v26) {
                        index$disjoined_datoms.__thunk__0__ = index$disjoined_datoms.__site__0__.fault(v25);
                        v26 = index$disjoined_datoms.__thunk__0__.get(v25);
                    }
                    if (v23 == (v27 = v23.get(v26))) {
                        index$disjoined_datoms.__thunk__1__ = index$disjoined_datoms.__site__1__.fault(v26);
                        v27 = index$disjoined_datoms.__thunk__1__.get(v26);
                    }
                    if (v27 != null && v27 != Boolean.FALSE) {
                        v28 = more;
                        more = null;
                        v29 = result;
                        result = null;
                        result = v29;
                        G__15692 = v28;
                        continue;
                    }
                    v30 = more;
                    v31 = more;
                    more = null;
                    state = ((IFn)index$disjoined_datoms.const__12.getRawRoot()).invoke(sd1, v31);
                    v32 = ((IFn)index$disjoined_datoms.const__13.getRawRoot()).invoke(tier, state);
                    if (v32 != null && v32 != Boolean.FALSE) {
                        v33 = (IFn)index$disjoined_datoms.const__14.getRawRoot();
                        v34 = result;
                        result = null;
                        v35 = state;
                        state = null;
                        v36 = Tuple.create((Object)v35);
                        v37 = index$disjoined_datoms.const__15.getRawRoot();
                        v38 = index$disjoined_datoms.__thunk__2__;
                        v39 = sd1;
                        sd1 = null;
                        v40 = v38.get(v39);
                        if (v38 == v40) {
                            index$disjoined_datoms.__thunk__2__ = index$disjoined_datoms.__site__2__.fault(v39);
                            v40 = index$disjoined_datoms.__thunk__2__.get(v39);
                        }
                        v41 /* !! */  = v33.invoke((Object)v34, (Object)v36, v37, v40);
                    } else {
                        v41 /* !! */  = result;
                        result = null;
                    }
                    result = v41 /* !! */ ;
                    G__15692 = v30;
                }
            }
            v22 = null;
        }
        return v22;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$disjoined_datoms.invokeStatic(object4, object5, object6);
    }

    static {
        const__1 = RT.var((String)"datomic.index", (String)"seg+item-seq");
        const__2 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.var((String)"clojure.core", (String)"first");
        const__4 = RT.var((String)"clojure.core", (String)"next");
        const__6 = RT.keyword(null, (String)"history");
        const__7 = RT.keyword(null, (String)"segmented");
        const__8 = RT.keyword(null, (String)"separated");
        const__9 = RT.keyword(null, (String)"absent");
        const__12 = RT.var((String)"datomic.index", (String)"paired-assertion-state");
        const__13 = RT.var((String)"datomic.index", (String)"problem-assertion-state?");
        const__14 = RT.var((String)"clojure.core", (String)"update-in");
        const__15 = RT.var((String)"clojure.core", (String)"conj");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"added"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
        __thunk__2__ = __site__2__;
    }
}

