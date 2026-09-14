/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ProcessExpander$fn__14029
extends AFunction {
    Object ids;
    Object local_tempids;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"calc-tempids");

    public ProcessExpander$fn__14029(Object object, Object object2) {
        this.ids = object;
        this.local_tempids = object2;
    }

    public Object invoke() {
        ProcessExpander$fn__14029 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.local_tempids, this_.ids);
    }
}

