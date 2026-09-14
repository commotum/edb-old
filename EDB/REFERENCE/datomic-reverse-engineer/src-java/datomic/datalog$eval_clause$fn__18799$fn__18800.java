/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$eval_clause$fn__18799$fn__18800
extends AFunction {
    Object oprog;
    Object ins;
    Object starts;
    Object db;
    Object top_bounds;
    Object inrel;
    Object sched_fn;
    Object whiles;
    Object prog;
    Object ans;
    Object apred;
    Object inbinds;
    Object outbinds;
    Object src;
    Object consts;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"eval-query");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"project");
    public static final Keyword const__2 = RT.keyword(null, (String)"consts");
    public static final Keyword const__3 = RT.keyword(null, (String)"starts");
    public static final Keyword const__4 = RT.keyword(null, (String)"whiles");

    public datalog$eval_clause$fn__18799$fn__18800(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15) {
        this.oprog = object;
        this.ins = object2;
        this.starts = object3;
        this.db = object4;
        this.top_bounds = object5;
        this.inrel = object6;
        this.sched_fn = object7;
        this.whiles = object8;
        this.prog = object9;
        this.ans = object10;
        this.apred = object11;
        this.inbinds = object12;
        this.outbinds = object13;
        this.src = object14;
        this.consts = object15;
    }

    public Object invoke() {
        Object object = this_.top_bounds;
        datalog$eval_clause$fn__18799$fn__18800 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.prog, this_.oprog, this_.apred, ((IFn)const__1.getRawRoot()).invoke(this_.inrel, this_.inbinds, this_.outbinds), this_.sched_fn, this_.src, this_.ans, this_.ins, null, object != null && object != Boolean.FALSE ? RT.mapUniqueKeys((Object[])new Object[]{const__2, this_.consts, const__3, this_.starts, const__4, this_.whiles}) : null);
    }
}

