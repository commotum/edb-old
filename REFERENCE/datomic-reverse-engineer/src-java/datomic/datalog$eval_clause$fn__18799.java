/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$eval_clause$fn__18799$fn__18800;
import datomic.datalog.ExtRel;
import datomic.datalog.IJoin;

public final class datalog$eval_clause$fn__18799
extends AFunction {
    Object join;
    Object oprog;
    Object ins;
    Object starts;
    Object ext_QMARK_;
    Object db;
    Object top_bounds;
    Object inrel;
    Object sched_fn;
    Object clause;
    Object whiles;
    Object projy;
    Object exf;
    Object exp;
    Object prog;
    Object ans;
    Object predctor;
    Object projx;
    Object root_QMARK_;
    Object inbinds;
    Object src;
    Object consts;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Var const__14;

    public datalog$eval_clause$fn__18799(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15, Object object16, Object object17, Object object18, Object object19, Object object20, Object object21, Object object22) {
        this.join = object;
        this.oprog = object2;
        this.ins = object3;
        this.starts = object4;
        this.ext_QMARK_ = object5;
        this.db = object6;
        this.top_bounds = object7;
        this.inrel = object8;
        this.sched_fn = object9;
        this.clause = object10;
        this.whiles = object11;
        this.projy = object12;
        this.exf = object13;
        this.exp = object14;
        this.prog = object15;
        this.ans = object16;
        this.predctor = object17;
        this.projx = object18;
        this.root_QMARK_ = object19;
        this.inbinds = object20;
        this.src = object21;
        this.consts = object22;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            block24: {
                block27: {
                    block26: {
                        block25: {
                            block21: {
                                block23: {
                                    block22: {
                                        v0 = this.ext_QMARK_;
                                        if (v0 == null || v0 == Boolean.FALSE) break block21;
                                        v1 = and__5236__auto__18803 = this.root_QMARK_;
                                        if (v1 != null && v1 != Boolean.FALSE) {
                                            v2 = ((IFn)datalog$eval_clause$fn__18799.const__1.getRawRoot()).invoke(this.inrel);
                                        } else {
                                            v2 = and__5236__auto__18803;
                                            and__5236__auto__18803 = null;
                                        }
                                        if (Util.classOf((Object)(v3 = v2 != null && v2 != Boolean.FALSE ? RT.set((Object[])new Object[]{PersistentVector.EMPTY}) : this.inrel)) == datalog$eval_clause$fn__18799.__cached_class__1) break block22;
                                        if (v3 instanceof IJoin) break block23;
                                        v3 = v3;
                                        datalog$eval_clause$fn__18799.__cached_class__1 = Util.classOf((Object)v3);
                                    }
                                    if (Util.classOf((Object)(v4 = this.db)) == datalog$eval_clause$fn__18799.__cached_class__0) ** GOTO lbl19
                                    if (!(v4 instanceof ExtRel)) {
                                        v4 = v4;
                                        datalog$eval_clause$fn__18799.__cached_class__0 = Util.classOf((Object)v4);
lbl19:
                                        // 2 sources

                                        v5 = datalog$eval_clause$fn__18799.const__2.getRawRoot().invoke(v4, this.consts, this.starts, this.whiles);
                                    } else {
                                        v5 = ((ExtRel)v4).extrel(this.consts, this.starts, this.whiles);
                                    }
                                    v6 = datalog$eval_clause$fn__18799.const__0.getRawRoot().invoke(v3, v5, this.join, this.projx, this.projy, this.predctor);
                                    break block24;
                                }
                                v7 = (IJoin)v3;
                                v8 = this.db;
                                if (Util.classOf((Object)v8) == datalog$eval_clause$fn__18799.__cached_class__0) ** GOTO lbl31
                                if (!(v8 instanceof ExtRel)) {
                                    v8 = v8;
                                    datalog$eval_clause$fn__18799.__cached_class__0 = Util.classOf((Object)v8);
lbl31:
                                    // 2 sources

                                    v9 = datalog$eval_clause$fn__18799.const__2.getRawRoot().invoke(v8, this.consts, this.starts, this.whiles);
                                } else {
                                    v9 = ((ExtRel)v8).extrel(this.consts, this.starts, this.whiles);
                                }
                                v6 = v7.join_project(v9, this.join, this.projx, this.projy, this.predctor);
                                break block24;
                            }
                            v10 = this.exf;
                            if (v10 == null || v10 == Boolean.FALSE) break block25;
                            v11 = and__5236__auto__18804 = this.root_QMARK_;
                            if (v11 != null && v11 != Boolean.FALSE) {
                                v12 = ((IFn)datalog$eval_clause$fn__18799.const__1.getRawRoot()).invoke(this.inrel);
                            } else {
                                v12 = and__5236__auto__18804;
                                and__5236__auto__18804 = null;
                            }
                            if (Util.classOf((Object)(v13 = v12 != null && v12 != Boolean.FALSE ? RT.set((Object[])new Object[]{PersistentVector.EMPTY}) : this.inrel)) == datalog$eval_clause$fn__18799.__cached_class__2) ** GOTO lbl49
                            if (!(v13 instanceof IJoin)) {
                                v13 = v13;
                                datalog$eval_clause$fn__18799.__cached_class__2 = Util.classOf((Object)v13);
lbl49:
                                // 2 sources

                                v6 = datalog$eval_clause$fn__18799.const__0.getRawRoot().invoke(v13, ((IFn)datalog$eval_clause$fn__18799.const__3.getRawRoot()).invoke(this.db, this.clause, this.consts), this.join, this.projx, this.projy, this.predctor);
                            } else {
                                v6 = ((IJoin)v13).join_project(((IFn)datalog$eval_clause$fn__18799.const__3.getRawRoot()).invoke(this.db, this.clause, this.consts), this.join, this.projx, this.projy, this.predctor);
                            }
                            break block24;
                        }
                        v14 = this.exp;
                        if (v14 == null || v14 == Boolean.FALSE) break block26;
                        v15 = and__5236__auto__18805 = this.root_QMARK_;
                        if (v15 != null && v15 != Boolean.FALSE) {
                            v16 = ((IFn)datalog$eval_clause$fn__18799.const__1.getRawRoot()).invoke(this.inrel);
                        } else {
                            v16 = and__5236__auto__18805;
                            and__5236__auto__18805 = null;
                        }
                        if (Util.classOf((Object)(v17 = v16 != null && v16 != Boolean.FALSE ? RT.set((Object[])new Object[]{PersistentVector.EMPTY}) : this.inrel)) == datalog$eval_clause$fn__18799.__cached_class__3) ** GOTO lbl66
                        if (!(v17 instanceof IJoin)) {
                            v17 = v17;
                            datalog$eval_clause$fn__18799.__cached_class__3 = Util.classOf((Object)v17);
lbl66:
                            // 2 sources

                            v6 = datalog$eval_clause$fn__18799.const__0.getRawRoot().invoke(v17, ((IFn)datalog$eval_clause$fn__18799.const__4.getRawRoot()).invoke(this.db, this.clause, this.consts), this.join, this.projx, this.projy, null);
                        } else {
                            v6 = ((IJoin)v17).join_project(((IFn)datalog$eval_clause$fn__18799.const__4.getRawRoot()).invoke(this.db, this.clause, this.consts), this.join, this.projx, this.projy, null);
                        }
                        break block24;
                    }
                    v18 = RT.get((Object)this.prog, (Object)((IFn)datalog$eval_clause$fn__18799.const__6.getRawRoot()).invoke(this.clause));
                    if (v18 == null || v18 == Boolean.FALSE) break block27;
                    bindset = ((IFn)datalog$eval_clause$fn__18799.const__7.getRawRoot()).invoke(this.inbinds);
                    apred = ((IFn)datalog$eval_clause$fn__18799.const__8.getRawRoot()).invoke(this.clause, bindset);
                    v19 = bindset;
                    bindset = null;
                    outbinds = ((IFn)datalog$eval_clause$fn__18799.const__9.getRawRoot()).invoke(v19, ((IFn)datalog$eval_clause$fn__18799.const__9.getRawRoot()).invoke(datalog$eval_clause$fn__18799.const__10.getRawRoot(), ((IFn)datalog$eval_clause$fn__18799.const__11.getRawRoot()).invoke(this.clause)));
                    v20 = pred = ((IFn)datalog$eval_clause$fn__18799.const__6.getRawRoot()).invoke(this.clause);
                    pred = null;
                    aresk = Tuple.create((Object)this.src, (Object)v20);
                    v21 = apred;
                    apred = null;
                    v22 = outbinds;
                    outbinds = null;
                    ((IFn)datalog$eval_clause$fn__18799.const__12.getRawRoot()).invoke((Object)new datalog$eval_clause$fn__18799$fn__18800(this.oprog, this.ins, this.starts, this.db, this.top_bounds, this.inrel, this.sched_fn, this.whiles, this.prog, this.ans, v21, this.inbinds, v22, this.src, this.consts));
                    v23 = and__5236__auto__18806 = this.root_QMARK_;
                    if (v23 != null && v23 != Boolean.FALSE) {
                        v24 = ((IFn)datalog$eval_clause$fn__18799.const__1.getRawRoot()).invoke(this.inrel);
                    } else {
                        v24 = and__5236__auto__18806;
                        and__5236__auto__18806 = null;
                    }
                    if (Util.classOf((Object)(v25 = v24 != null && v24 != Boolean.FALSE ? RT.set((Object[])new Object[]{PersistentVector.EMPTY}) : this.inrel)) == datalog$eval_clause$fn__18799.__cached_class__4) ** GOTO lbl97
                    if (!(v25 instanceof IJoin)) {
                        v25 = v25;
                        datalog$eval_clause$fn__18799.__cached_class__4 = Util.classOf((Object)v25);
lbl97:
                        // 2 sources

                        v26 = aresk;
                        aresk = null;
                        v6 = datalog$eval_clause$fn__18799.const__0.getRawRoot().invoke(v25, RT.get((Object)this.ans, (Object)v26, (Object)PersistentVector.EMPTY), this.join, this.projx, this.projy, this.predctor);
                    } else {
                        v27 = aresk;
                        aresk = null;
                        v6 = ((IJoin)v25).join_project(RT.get((Object)this.ans, (Object)v27, (Object)PersistentVector.EMPTY), this.join, this.projx, this.projy, this.predctor);
                    }
                    break block24;
                }
                v28 = datalog$eval_clause$fn__18799.const__13;
                if (v28 != null && v28 != Boolean.FALSE) {
                    throw (Throwable)new IllegalArgumentException((String)((IFn)datalog$eval_clause$fn__18799.const__14.getRawRoot()).invoke((Object)"Undefined predicate: ", ((IFn)datalog$eval_clause$fn__18799.const__6.getRawRoot()).invoke(this.clause)));
                }
                v6 = null;
            }
            var7_9 = v6;
        }
        catch (Exception ex) {
            ex = null;
            throw (Throwable)new Exception((String)((IFn)datalog$eval_clause$fn__18799.const__14.getRawRoot()).invoke((Object)"processing clause: ", this.clause, (Object)", message: ", (Object)((Throwable)ex).getMessage()), ex);
        }
        return var7_9;
    }

    static {
        const__0 = RT.var((String)"datomic.datalog", (String)"join-project");
        const__1 = RT.var((String)"clojure.core", (String)"empty?");
        const__2 = RT.var((String)"datomic.datalog", (String)"extrel");
        const__3 = RT.var((String)"datomic.datalog", (String)"fnrel");
        const__4 = RT.var((String)"datomic.datalog", (String)"predrel");
        const__6 = RT.var((String)"clojure.core", (String)"first");
        const__7 = RT.var((String)"clojure.core", (String)"set");
        const__8 = RT.var((String)"datomic.datalog", (String)"adorned-pred");
        const__9 = RT.var((String)"clojure.core", (String)"filter");
        const__10 = RT.var((String)"datomic.datalog", (String)"variable?");
        const__11 = RT.var((String)"clojure.core", (String)"next");
        const__12 = RT.var((String)"datomic.measure.query-stats", (String)"with-phase-stats");
        const__13 = RT.keyword(null, (String)"else");
        const__14 = RT.var((String)"clojure.core", (String)"str");
    }
}

