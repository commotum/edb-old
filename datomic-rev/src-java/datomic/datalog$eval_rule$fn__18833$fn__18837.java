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

public final class datalog$eval_rule$fn__18833$fn__18837
extends AFunction {
    Object ans;
    Object predctor;
    Object oprog;
    Object top_bounds;
    Object srcs;
    Object next_binds;
    Object ins;
    Object c;
    Object src;
    Object cdb;
    Object csrc;
    Object prog;
    Object sup;
    Object sched_fn;
    Object sbinds;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"eval-clause");

    public datalog$eval_rule$fn__18833$fn__18837(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15) {
        this.ans = object;
        this.predctor = object2;
        this.oprog = object3;
        this.top_bounds = object4;
        this.srcs = object5;
        this.next_binds = object6;
        this.ins = object7;
        this.c = object8;
        this.src = object9;
        this.cdb = object10;
        this.csrc = object11;
        this.prog = object12;
        this.sup = object13;
        this.sched_fn = object14;
        this.sbinds = object15;
    }

    public Object invoke() {
        Object object;
        Object or__5238__auto__18839;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = or__5238__auto__18839 = this_.csrc;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__18839;
            or__5238__auto__18839 = null;
        } else {
            object = this_.src;
        }
        datalog$eval_rule$fn__18833$fn__18837 this_ = null;
        return iFn.invoke(this_.cdb, this_.srcs, this_.prog, this_.oprog, this_.c, this_.predctor, this_.sup, this_.sbinds, this_.next_binds, this_.sched_fn, object, this_.ans, this_.ins, this_.top_bounds);
    }
}

