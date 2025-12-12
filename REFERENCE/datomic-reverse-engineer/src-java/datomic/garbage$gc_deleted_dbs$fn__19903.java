/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class garbage$gc_deleted_dbs$fn__19903
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__20;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object n, Object p__19902) {
        v0 = p__19902;
        p__19902 = null;
        vec__19904 = v0;
        cluster = RT.nth((Object)vec__19904, (int)RT.intCast((long)0L), null);
        v1 = vec__19904;
        vec__19904 = null;
        garbage = RT.nth((Object)v1, (int)RT.intCast((long)1L), null);
        seq_19907 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__3.getRawRoot()).invoke(garbage);
        chunk_19908 = null;
        count_19909 = 0L;
        i_19910 = 0L;
        while (true) {
            block16: {
                if (i_19910 >= count_19909) break block16;
                g = ((Indexed)chunk_19908).nth(RT.intCast((long)i_19910));
                v2 = (IFn)garbage$gc_deleted_dbs$fn__19903.const__3.getRawRoot();
                v3 = garbage$gc_deleted_dbs$fn__19903.__thunk__0__;
                v4 = g;
                g = null;
                v5 = v3.get(v4);
                if (v3 == v5) {
                    garbage$gc_deleted_dbs$fn__19903.__thunk__0__ = garbage$gc_deleted_dbs$fn__19903.__site__0__.fault(v4);
                    v5 = garbage$gc_deleted_dbs$fn__19903.__thunk__0__.get(v4);
                }
                seq_19911 = v2.invoke(v5);
                chunk_19912 = null;
                count_19913 = 0L;
                i_19914 = 0L;
                while (true) {
                    block17: {
                        if (i_19914 >= count_19913) break block17;
                        v = ((Indexed)chunk_19912).nth(RT.intCast((long)i_19914));
                        ((IFn)garbage$gc_deleted_dbs$fn__19903.const__6.getRawRoot()).invoke();
                        v6 = (IFn)garbage$gc_deleted_dbs$fn__19903.const__7.getRawRoot();
                        v7 = cluster;
                        if (Util.classOf((Object)v7) == garbage$gc_deleted_dbs$fn__19903.__cached_class__0) ** GOTO lbl39
                        if (!(v7 instanceof ClusteredStore)) {
                            v7 = v7;
                            garbage$gc_deleted_dbs$fn__19903.__cached_class__0 = Util.classOf((Object)v7);
lbl39:
                            // 2 sources

                            v8 = v;
                            v = null;
                            v9 = garbage$gc_deleted_dbs$fn__19903.const__8.getRawRoot().invoke(v7, v8);
                        } else {
                            v10 = v;
                            v = null;
                            v9 = ((ClusteredStore)v7).delete(v10);
                        }
                        v6.invoke(v9);
                        v11 = seq_19911;
                        seq_19911 = null;
                        v12 = chunk_19912;
                        chunk_19912 = null;
                        ++i_19914;
                        chunk_19912 = v12;
                        seq_19911 = v11;
                        continue;
                    }
                    v13 = seq_19911;
                    seq_19911 = null;
                    v14 = temp__5457__auto__19921 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__3.getRawRoot()).invoke(v13);
                    if (v14 == null || v14 == Boolean.FALSE) break;
                    v15 = temp__5457__auto__19921;
                    temp__5457__auto__19921 = null;
                    seq_19911 = v15;
                    v16 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__10.getRawRoot()).invoke(seq_19911);
                    if (v16 != null && v16 != Boolean.FALSE) {
                        c__5719__auto__19920 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__11.getRawRoot()).invoke(seq_19911);
                        v17 = seq_19911;
                        seq_19911 = null;
                        v18 = c__5719__auto__19920;
                        v19 = c__5719__auto__19920;
                        c__5719__auto__19920 = null;
                        i_19914 = RT.intCast((long)0L);
                        count_19913 = RT.intCast((int)RT.count((Object)v19));
                        chunk_19912 = v18;
                        seq_19911 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__12.getRawRoot()).invoke(v17);
                        continue;
                    }
                    v = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__15.getRawRoot()).invoke(seq_19911);
                    ((IFn)garbage$gc_deleted_dbs$fn__19903.const__6.getRawRoot()).invoke();
                    v20 = (IFn)garbage$gc_deleted_dbs$fn__19903.const__7.getRawRoot();
                    v21 = cluster;
                    if (Util.classOf((Object)v21) == garbage$gc_deleted_dbs$fn__19903.__cached_class__1) ** GOTO lbl86
                    if (!(v21 instanceof ClusteredStore)) {
                        v21 = v21;
                        garbage$gc_deleted_dbs$fn__19903.__cached_class__1 = Util.classOf((Object)v21);
lbl86:
                        // 2 sources

                        v22 = v;
                        v = null;
                        v23 = garbage$gc_deleted_dbs$fn__19903.const__8.getRawRoot().invoke(v21, v22);
                    } else {
                        v24 = v;
                        v = null;
                        v23 = ((ClusteredStore)v21).delete(v24);
                    }
                    v20.invoke(v23);
                    v25 = seq_19911;
                    seq_19911 = null;
                    i_19914 = 0L;
                    count_19913 = 0L;
                    chunk_19912 = null;
                    seq_19911 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__16.getRawRoot()).invoke(v25);
                }
                v26 = seq_19907;
                seq_19907 = null;
                v27 = chunk_19908;
                chunk_19908 = null;
                ++i_19910;
                chunk_19908 = v27;
                seq_19907 = v26;
                continue;
            }
            v28 = seq_19907;
            seq_19907 = null;
            v29 = temp__5457__auto__19925 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__3.getRawRoot()).invoke(v28);
            if (v29 == null || v29 == Boolean.FALSE) break;
            v30 = temp__5457__auto__19925;
            temp__5457__auto__19925 = null;
            seq_19907 = v30;
            v31 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__10.getRawRoot()).invoke(seq_19907);
            if (v31 != null && v31 != Boolean.FALSE) {
                c__5719__auto__19922 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__11.getRawRoot()).invoke(seq_19907);
                v32 = seq_19907;
                seq_19907 = null;
                v33 = c__5719__auto__19922;
                v34 = c__5719__auto__19922;
                c__5719__auto__19922 = null;
                i_19910 = RT.intCast((long)0L);
                count_19909 = RT.intCast((int)RT.count((Object)v34));
                chunk_19908 = v33;
                seq_19907 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__12.getRawRoot()).invoke(v32);
                continue;
            }
            g = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__15.getRawRoot()).invoke(seq_19907);
            v35 = (IFn)garbage$gc_deleted_dbs$fn__19903.const__3.getRawRoot();
            v36 = garbage$gc_deleted_dbs$fn__19903.__thunk__1__;
            v37 = g;
            g = null;
            v38 = v36.get(v37);
            if (v36 == v38) {
                garbage$gc_deleted_dbs$fn__19903.__thunk__1__ = garbage$gc_deleted_dbs$fn__19903.__site__1__.fault(v37);
                v38 = garbage$gc_deleted_dbs$fn__19903.__thunk__1__.get(v37);
            }
            seq_19915 = v35.invoke(v38);
            chunk_19916 = null;
            count_19917 = 0L;
            i_19918 = 0L;
            while (true) {
                block18: {
                    if (i_19918 >= count_19917) break block18;
                    v = ((Indexed)chunk_19916).nth(RT.intCast((long)i_19918));
                    ((IFn)garbage$gc_deleted_dbs$fn__19903.const__6.getRawRoot()).invoke();
                    v39 = (IFn)garbage$gc_deleted_dbs$fn__19903.const__7.getRawRoot();
                    v40 = cluster;
                    if (Util.classOf((Object)v40) == garbage$gc_deleted_dbs$fn__19903.__cached_class__2) ** GOTO lbl157
                    if (!(v40 instanceof ClusteredStore)) {
                        v40 = v40;
                        garbage$gc_deleted_dbs$fn__19903.__cached_class__2 = Util.classOf((Object)v40);
lbl157:
                        // 2 sources

                        v41 = v;
                        v = null;
                        v42 = garbage$gc_deleted_dbs$fn__19903.const__8.getRawRoot().invoke(v40, v41);
                    } else {
                        v43 = v;
                        v = null;
                        v42 = ((ClusteredStore)v40).delete(v43);
                    }
                    v39.invoke(v42);
                    v44 = seq_19915;
                    seq_19915 = null;
                    v45 = chunk_19916;
                    chunk_19916 = null;
                    ++i_19918;
                    chunk_19916 = v45;
                    seq_19915 = v44;
                    continue;
                }
                v46 = seq_19915;
                seq_19915 = null;
                v47 = temp__5457__auto__19924 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__3.getRawRoot()).invoke(v46);
                if (v47 == null || v47 == Boolean.FALSE) break;
                v48 = temp__5457__auto__19924;
                temp__5457__auto__19924 = null;
                seq_19915 = v48;
                v49 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__10.getRawRoot()).invoke(seq_19915);
                if (v49 != null && v49 != Boolean.FALSE) {
                    c__5719__auto__19923 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__11.getRawRoot()).invoke(seq_19915);
                    v50 = seq_19915;
                    seq_19915 = null;
                    v51 = c__5719__auto__19923;
                    v52 = c__5719__auto__19923;
                    c__5719__auto__19923 = null;
                    i_19918 = RT.intCast((long)0L);
                    count_19917 = RT.intCast((int)RT.count((Object)v52));
                    chunk_19916 = v51;
                    seq_19915 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__12.getRawRoot()).invoke(v50);
                    continue;
                }
                v = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__15.getRawRoot()).invoke(seq_19915);
                ((IFn)garbage$gc_deleted_dbs$fn__19903.const__6.getRawRoot()).invoke();
                v53 = (IFn)garbage$gc_deleted_dbs$fn__19903.const__7.getRawRoot();
                v54 = cluster;
                if (Util.classOf((Object)v54) == garbage$gc_deleted_dbs$fn__19903.__cached_class__3) ** GOTO lbl204
                if (!(v54 instanceof ClusteredStore)) {
                    v54 = v54;
                    garbage$gc_deleted_dbs$fn__19903.__cached_class__3 = Util.classOf((Object)v54);
lbl204:
                    // 2 sources

                    v55 = v;
                    v = null;
                    v56 = garbage$gc_deleted_dbs$fn__19903.const__8.getRawRoot().invoke(v54, v55);
                } else {
                    v57 = v;
                    v = null;
                    v56 = ((ClusteredStore)v54).delete(v57);
                }
                v53.invoke(v56);
                v58 = seq_19915;
                seq_19915 = null;
                i_19918 = 0L;
                count_19917 = 0L;
                chunk_19916 = null;
                seq_19915 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__16.getRawRoot()).invoke(v58);
            }
            v59 = seq_19907;
            seq_19907 = null;
            i_19910 = 0L;
            count_19909 = 0L;
            chunk_19908 = null;
            seq_19907 = ((IFn)garbage$gc_deleted_dbs$fn__19903.const__16.getRawRoot()).invoke(v59);
        }
        v60 = n;
        n = null;
        v61 = garbage;
        garbage = null;
        this = null;
        return ((IFn)garbage$gc_deleted_dbs$fn__19903.const__17.getRawRoot()).invoke(garbage$gc_deleted_dbs$fn__19903.const__18.getRawRoot(), v60, ((IFn)garbage$gc_deleted_dbs$fn__19903.const__19.getRawRoot()).invoke(((IFn)garbage$gc_deleted_dbs$fn__19903.const__20.getRawRoot()).invoke(garbage$gc_deleted_dbs$fn__19903.const__14.getRawRoot(), (Object)garbage$gc_deleted_dbs$fn__19903.const__5), v61));
    }

    static {
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"vals");
        const__6 = RT.var((String)"datomic.garbage", (String)"pace-gc");
        const__7 = RT.var((String)"clojure.core", (String)"deref");
        const__8 = RT.var((String)"datomic.cluster", (String)"delete");
        const__10 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__11 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__12 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__14 = RT.var((String)"clojure.core", (String)"count");
        const__15 = RT.var((String)"clojure.core", (String)"first");
        const__16 = RT.var((String)"clojure.core", (String)"next");
        const__17 = RT.var((String)"clojure.core", (String)"apply");
        const__18 = RT.var((String)"clojure.core", (String)"+");
        const__19 = RT.var((String)"clojure.core", (String)"map");
        const__20 = RT.var((String)"clojure.core", (String)"comp");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"vals"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"vals"));
        __thunk__1__ = __site__1__;
    }
}

