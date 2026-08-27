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

public final class db$distinct_last_by$step__12863$fn__12867
extends AFunction {
    Object step;
    Object cur;
    Object f;
    Object more;
    Object prior;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");

    public db$distinct_last_by$step__12863$fn__12867(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.step = object;
        this.cur = object2;
        this.f = object3;
        this.more = object4;
        this.prior = object5;
    }

    public Object invoke() {
        db$distinct_last_by$step__12863$fn__12867 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.prior, ((IFn)this_.step).invoke(this_.f, this_.cur, this_.more));
    }
}

