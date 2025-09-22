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
import java.util.concurrent.atomic.LongAdder;

public final class db$empty_tx_stat_registers$fn__13406
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc!");

    public Object invoke(Object m, Object n) {
        Object object = m;
        m = null;
        Object object2 = n;
        n = null;
        db$empty_tx_stat_registers$fn__13406 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)new LongAdder());
    }
}

