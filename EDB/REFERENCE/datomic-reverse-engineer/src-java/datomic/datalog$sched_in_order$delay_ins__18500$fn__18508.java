/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$sched_in_order$delay_ins__18500$fn__18508
extends AFunction {
    Object m;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"find");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"next");

    public datalog$sched_in_order$delay_ins__18500$fn__18508(Object object) {
        this.m = object;
    }

    public Object invoke(Object ret, Object c) {
        Object object;
        datalog$sched_in_order$delay_ins__18500$fn__18508 this_;
        Object vec__18509 = ((IFn)const__0.getRawRoot()).invoke(this_.m, c);
        Object k = RT.nth((Object)vec__18509, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__18509;
        vec__18509 = null;
        Object vs = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = k;
        k = null;
        if (Util.identical((Object)object3, (Object)c)) {
            Object object4 = ret;
            ret = null;
            Object object5 = c;
            c = null;
            Object object6 = ((IFn)const__6.getRawRoot()).invoke(object4, ((IFn)const__7.getRawRoot()).invoke(vs), object5);
            Object object7 = vs;
            vs = null;
            this_ = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object6, ((IFn)const__8.getRawRoot()).invoke(object7));
        } else {
            Object object8 = ret;
            ret = null;
            Object object9 = c;
            c = null;
            this_ = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object8, object9);
        }
        return object;
    }
}

