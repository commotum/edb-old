/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.HashMap;

public final class datalog$qsqr$fn__18880
extends AFunction {
    Object prog;
    Object top_bounds;
    Object inrel;
    Object oprog;
    Object apred;
    Object db;
    Object ans;
    Object sched_fn;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"eval-query");

    public datalog$qsqr$fn__18880(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.prog = object;
        this.top_bounds = object2;
        this.inrel = object3;
        this.oprog = object4;
        this.apred = object5;
        this.db = object6;
        this.ans = object7;
        this.sched_fn = object8;
    }

    public Object invoke() {
        datalog$qsqr$fn__18880 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.prog, this_.oprog, this_.apred, this_.inrel, this_.sched_fn, null, this_.ans, new HashMap(), this_.top_bounds, null);
    }
}

