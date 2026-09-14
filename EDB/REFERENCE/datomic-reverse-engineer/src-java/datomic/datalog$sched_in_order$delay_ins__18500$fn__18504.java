/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$sched_in_order$delay_ins__18500$fn__18504$fn__18505;

public final class datalog$sched_in_order$delay_ins__18500$fn__18504
extends AFunction {
    Object cs;
    Object cbinds;
    Object cargs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"update-in");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"conj");

    public datalog$sched_in_order$delay_ins__18500$fn__18504(Object object, Object object2, Object object3) {
        this.cs = object;
        this.cbinds = object2;
        this.cargs = object3;
    }

    public Object invoke(Object m, Object in) {
        Object bset;
        Object object = bset = ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.cbinds).invoke(in));
        bset = null;
        Object uc = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new datalog$sched_in_order$delay_ins__18500$fn__18504$fn__18505(object, this_.cargs), this_.cs));
        Object object2 = m;
        m = null;
        Object object3 = uc;
        uc = null;
        Object object4 = in;
        in = null;
        datalog$sched_in_order$delay_ins__18500$fn__18504 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2, (Object)Tuple.create((Object)object3), const__4.getRawRoot(), object4);
    }
}

