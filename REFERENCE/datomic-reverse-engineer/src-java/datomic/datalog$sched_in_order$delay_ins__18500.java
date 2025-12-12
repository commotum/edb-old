/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datalog$sched_in_order$delay_ins__18500$fn__18504;
import datomic.datalog$sched_in_order$delay_ins__18500$fn__18508;

public final class datalog$sched_in_order$delay_ins__18500
extends AFunction {
    Object cbinds;
    Object in_clause_QMARK_;
    Object cargs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"split-with");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");

    public datalog$sched_in_order$delay_ins__18500(Object object, Object object2, Object object3) {
        this.cbinds = object;
        this.in_clause_QMARK_ = object2;
        this.cargs = object3;
    }

    public Object invoke(Object cs) {
        Object object = cs;
        cs = null;
        Object vec__18501 = ((IFn)const__0.getRawRoot()).invoke(this_.in_clause_QMARK_, object);
        Object ins = RT.nth((Object)vec__18501, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__18501;
        vec__18501 = null;
        Object cs2 = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = ins;
        ins = null;
        Object m = ((IFn)const__4.getRawRoot()).invoke((Object)new datalog$sched_in_order$delay_ins__18500$fn__18504(cs2, this_.cbinds, this_.cargs), (Object)PersistentArrayMap.EMPTY, object3);
        datalog$sched_in_order$delay_ins__18500$fn__18508 datalog$sched_in_order$delay_ins__18500$fn__18508 = new datalog$sched_in_order$delay_ins__18500$fn__18508(m);
        Object object4 = cs2;
        cs2 = null;
        Object object5 = m;
        m = null;
        datalog$sched_in_order$delay_ins__18500 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)datalog$sched_in_order$delay_ins__18500$fn__18508, (Object)PersistentVector.EMPTY, object4), RT.get((Object)object5, null));
    }
}

