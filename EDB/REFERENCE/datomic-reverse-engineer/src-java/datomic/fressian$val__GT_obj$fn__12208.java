/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.fressian$val__GT_obj$fn__12208$fn__12209;

public final class fressian$val__GT_obj$fn__12208
extends AFunction {
    Object read_lookup;
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"record-latencies");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"return-or-throw");

    public fressian$val__GT_obj$fn__12208(Object object) {
        this.read_lookup = object;
    }

    public Object invoke(Object val) {
        long start__9163__auto__12212 = System.nanoTime();
        Object object = val;
        val = null;
        Object result__9164__auto__12213 = ((IFn)new fressian$val__GT_obj$fn__12208$fn__12209(this_.read_lookup, object)).invoke();
        ((IFn)const__0.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.minus((long)System.nanoTime(), (long)start__9163__auto__12212)), result__9164__auto__12213);
        Object object2 = result__9164__auto__12213;
        result__9164__auto__12213 = null;
        fressian$val__GT_obj$fn__12208 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke(object2);
    }
}

