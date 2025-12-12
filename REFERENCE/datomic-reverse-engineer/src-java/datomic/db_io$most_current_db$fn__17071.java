/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;

public final class db_io$most_current_db$fn__17071
extends AFunction {
    Object basis;

    public db_io$most_current_db$fn__17071(Object object) {
        this.basis = object;
    }

    public Object invoke(Object db1, Object db2) {
        Object object;
        if (Numbers.lt((Object)((IFn)this.basis).invoke(db1), (Object)((IFn)this.basis).invoke(db2))) {
            object = db2;
            db2 = null;
        } else {
            object = db1;
            Object var1_1 = null;
        }
        return object;
    }
}

