/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$crosscheck_dir_segs$fn__22282$fn__22287$fn__22288;
import datomic.integrity$crosscheck_dir_segs$fn__22282$fn__22287$fn__22290;
import datomic.log.LogDirSeq;
import java.util.Arrays;

public final class integrity$crosscheck_dir_segs$fn__22282$fn__22287
extends AFunction {
    Object p__22277;
    Object olookup;
    Object log;
    Object map__22278;
    long start__8981__auto__;
    Object m_22279;
    Object tree_iter;
    Object cluster;
    Object ___8980__auto__;
    Object progress;
    Object temp__5457__auto__;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final AFn const__10;
    public static final AFn const__11;
    public static final AFn const__12;
    public static final AFn const__13;
    public static final AFn const__14;
    public static final AFn const__15;
    public static final AFn const__16;
    public static final AFn const__17;
    public static final AFn const__18;
    public static final AFn const__19;
    public static final AFn const__20;
    public static final AFn const__21;
    public static final AFn const__22;
    public static final AFn const__23;
    public static final AFn const__24;
    public static final AFn const__25;
    public static final AFn const__26;
    public static final Keyword const__27;
    public static final Var const__28;
    public static final Var const__30;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__35;
    public static final Var const__36;

    public integrity$crosscheck_dir_segs$fn__22282$fn__22287(Object object, Object object2, Object object3, Object object4, long l, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.p__22277 = object;
        this.olookup = object2;
        this.log = object3;
        this.map__22278 = object4;
        this.start__8981__auto__ = l;
        this.m_22279 = object5;
        this.tree_iter = object6;
        this.cluster = object7;
        this.___8980__auto__ = object8;
        this.progress = object9;
        this.temp__5457__auto__ = object10;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        v0 = (IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__0.getRawRoot();
        v1 = this.tree_iter;
        if (Util.classOf((Object)v1) == integrity$crosscheck_dir_segs$fn__22282$fn__22287.__cached_class__0) ** GOTO lbl7
        if (!(v1 instanceof LogDirSeq)) {
            v1 = v1;
            integrity$crosscheck_dir_segs$fn__22282$fn__22287.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__1.getRawRoot().invoke(v1);
        } else {
            v2 = ((LogDirSeq)v1).log_dir_seq();
        }
        seq_22283 = v0.invoke(v2);
        chunk_22284 = null;
        count_22285 = 0L;
        i_22286 = 0L;
        while (true) {
            if (i_22286 < count_22285) {
                adir = ((Indexed)chunk_22284).nth(RT.intCast((long)i_22286));
                v3 = this.progress;
                if (v3 != null && v3 != Boolean.FALSE) {
                    ((IFn)this.progress).invoke(adir);
                }
                bad_dirs = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__0.getRawRoot()).invoke(((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__4.getRawRoot()).invoke((Object)new integrity$crosscheck_dir_segs$fn__22282$fn__22287$fn__22288(), ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__5.getRawRoot()).invoke(adir, this.olookup)));
                v4 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__6.getRawRoot()).invoke(bad_dirs);
                if (v4 != null && v4 != Boolean.FALSE) {
                } else {
                    form__20659__auto__22293 = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__7;
                    v5 = new Object[4];
                    v5[0] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__9;
                    v6 = new Object[34];
                    v6[0] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__10;
                    v6[1] = this.p__22277;
                    v6[2] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__11;
                    v6[3] = this.olookup;
                    v6[4] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__12;
                    v6[5] = this.progress;
                    v6[6] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__13;
                    v6[7] = this.___8980__auto__;
                    v6[8] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__14;
                    v6[9] = this.m_22279;
                    v6[10] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__15;
                    v6[11] = this.log;
                    v6[12] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__16;
                    v7 = adir;
                    adir = null;
                    v6[13] = v7;
                    v6[14] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__17;
                    v6[15] = chunk_22284;
                    v6[16] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__18;
                    v6[17] = Numbers.num((long)count_22285);
                    v6[18] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__19;
                    v6[19] = Numbers.num((long)i_22286);
                    v6[20] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__20;
                    v6[21] = this.tree_iter;
                    v6[22] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__21;
                    v6[23] = seq_22283;
                    v6[24] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__22;
                    v6[25] = this.temp__5457__auto__;
                    v6[26] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__23;
                    v6[27] = Numbers.num((long)this.start__8981__auto__);
                    v6[28] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__24;
                    v8 = bad_dirs;
                    bad_dirs = null;
                    v6[29] = v8;
                    v6[30] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__25;
                    v6[31] = this.map__22278;
                    v6[32] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__26;
                    v6[33] = this.cluster;
                    v5[1] = RT.mapUniqueKeys((Object[])v6);
                    v5[2] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__27;
                    v9 = form__20659__auto__22293;
                    form__20659__auto__22293 = null;
                    v5[3] = v9;
                    error__20660__auto__22294 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__8.getRawRoot()).invoke((Object)"Assertion failed, see ex-data for details", (Object)RT.mapUniqueKeys((Object[])v5));
                    v10 = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__28.get();
                    if (v10 != null && v10 != Boolean.FALSE) {
                        v11 = error__20660__auto__22294;
                        error__20660__auto__22294 = null;
                        ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__28.get()).invoke(v11);
                    } else {
                        v12 = error__20660__auto__22294;
                        error__20660__auto__22294 = null;
                        throw (Throwable)v12;
                    }
                }
                v13 = seq_22283;
                seq_22283 = null;
                v14 = chunk_22284;
                chunk_22284 = null;
                ++i_22286;
                chunk_22284 = v14;
                seq_22283 = v13;
                continue;
            }
            v15 = seq_22283;
            seq_22283 = null;
            v16 = temp__5457__auto__22298 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__0.getRawRoot()).invoke(v15);
            if (v16 == null || v16 == Boolean.FALSE) break;
            seq_22283 = temp__5457__auto__22298;
            v17 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__30.getRawRoot()).invoke(seq_22283);
            if (v17 != null && v17 != Boolean.FALSE) {
                c__5719__auto__22295 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__31.getRawRoot()).invoke(seq_22283);
                v18 = seq_22283;
                seq_22283 = null;
                v19 = c__5719__auto__22295;
                v20 = c__5719__auto__22295;
                c__5719__auto__22295 = null;
                i_22286 = RT.intCast((long)0L);
                count_22285 = RT.intCast((int)RT.count((Object)v20));
                chunk_22284 = v19;
                seq_22283 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__32.getRawRoot()).invoke(v18);
                continue;
            }
            adir = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__35.getRawRoot()).invoke(seq_22283);
            v21 = this.progress;
            if (v21 != null && v21 != Boolean.FALSE) {
                ((IFn)this.progress).invoke(adir);
            }
            bad_dirs = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__0.getRawRoot()).invoke(((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__4.getRawRoot()).invoke((Object)new integrity$crosscheck_dir_segs$fn__22282$fn__22287$fn__22290(), ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__5.getRawRoot()).invoke(adir, this.olookup)));
            v22 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__6.getRawRoot()).invoke(bad_dirs);
            if (v22 != null && v22 != Boolean.FALSE) {
            } else {
                form__20659__auto__22296 = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__7;
                v23 = new Object[4];
                v23[0] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__9;
                v24 = new Object[34];
                v24[0] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__10;
                v24[1] = this.p__22277;
                v24[2] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__11;
                v24[3] = this.olookup;
                v24[4] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__12;
                v24[5] = this.progress;
                v24[6] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__13;
                v24[7] = this.___8980__auto__;
                v24[8] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__14;
                v24[9] = this.m_22279;
                v24[10] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__15;
                v24[11] = this.log;
                v24[12] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__16;
                v25 = adir;
                adir = null;
                v24[13] = v25;
                v24[14] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__17;
                v26 = chunk_22284;
                chunk_22284 = null;
                v24[15] = v26;
                v24[16] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__18;
                v24[17] = Numbers.num((long)count_22285);
                v24[18] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__19;
                v24[19] = Numbers.num((long)i_22286);
                v24[20] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__20;
                v24[21] = this.tree_iter;
                v24[22] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__21;
                v24[23] = seq_22283;
                v24[24] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__22;
                v27 = temp__5457__auto__22298;
                temp__5457__auto__22298 = null;
                v24[25] = v27;
                v24[26] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__23;
                v24[27] = Numbers.num((long)this.start__8981__auto__);
                v24[28] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__24;
                v28 = bad_dirs;
                bad_dirs = null;
                v24[29] = v28;
                v24[30] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__25;
                v24[31] = this.map__22278;
                v24[32] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__26;
                v24[33] = this.cluster;
                v23[1] = RT.mapUniqueKeys((Object[])v24);
                v23[2] = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__27;
                v29 = form__20659__auto__22296;
                form__20659__auto__22296 = null;
                v23[3] = v29;
                error__20660__auto__22297 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__8.getRawRoot()).invoke((Object)"Assertion failed, see ex-data for details", (Object)RT.mapUniqueKeys((Object[])v23));
                v30 = integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__28.get();
                if (v30 != null && v30 != Boolean.FALSE) {
                    v31 = error__20660__auto__22297;
                    error__20660__auto__22297 = null;
                    ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__28.get()).invoke(v31);
                } else {
                    v32 = error__20660__auto__22297;
                    error__20660__auto__22297 = null;
                    throw (Throwable)v32;
                }
            }
            v33 = seq_22283;
            seq_22283 = null;
            i_22286 = 0L;
            count_22285 = 0L;
            chunk_22284 = null;
            seq_22283 = ((IFn)integrity$crosscheck_dir_segs$fn__22282$fn__22287.const__36.getRawRoot()).invoke(v33);
        }
        return null;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq");
        const__1 = RT.var((String)"datomic.log", (String)"log-dir-seq");
        const__4 = RT.var((String)"clojure.core", (String)"remove");
        const__5 = RT.var((String)"datomic.integrity", (String)"dir-seg-info-seq");
        const__6 = RT.var((String)"clojure.core", (String)"not");
        const__7 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"not"), Symbol.intern(null, (String)"bad-dirs")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 20}));
        const__8 = RT.var((String)"clojure.core", (String)"ex-info");
        const__9 = RT.keyword(null, (String)"bindings");
        const__10 = (AFn)Symbol.intern(null, (String)"p__22277");
        const__11 = (AFn)Symbol.intern(null, (String)"olookup");
        const__12 = (AFn)Symbol.intern(null, (String)"progress");
        const__13 = (AFn)Symbol.intern(null, (String)"___8980__auto__");
        const__14 = (AFn)Symbol.intern(null, (String)"m_22279");
        const__15 = (AFn)Symbol.intern(null, (String)"log");
        const__16 = (AFn)Symbol.intern(null, (String)"adir");
        const__17 = (AFn)((IObj)Symbol.intern(null, (String)"chunk_22284")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"clojure.lang.IChunk")}));
        const__18 = (AFn)Symbol.intern(null, (String)"count_22285");
        const__19 = (AFn)Symbol.intern(null, (String)"i_22286");
        const__20 = (AFn)Symbol.intern(null, (String)"tree-iter");
        const__21 = (AFn)Symbol.intern(null, (String)"seq_22283");
        const__22 = (AFn)Symbol.intern(null, (String)"temp__5457__auto__");
        const__23 = (AFn)Symbol.intern(null, (String)"start__8981__auto__");
        const__24 = (AFn)Symbol.intern(null, (String)"bad-dirs");
        const__25 = (AFn)Symbol.intern(null, (String)"map__22278");
        const__26 = (AFn)Symbol.intern(null, (String)"cluster");
        const__27 = RT.keyword(null, (String)"form");
        const__28 = RT.var((String)"datomic.assert", (String)"*assert-handler*");
        const__30 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__31 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__32 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__35 = RT.var((String)"clojure.core", (String)"first");
        const__36 = RT.var((String)"clojure.core", (String)"next");
    }
}

