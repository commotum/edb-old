/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Tuple;

public final class datalog$sched_in_order$pack__18473
extends AFunction {
    Object src;

    public datalog$sched_in_order$pack__18473(Object object) {
        this.src = object;
    }

    public Object invoke(Object c) {
        Object object = ((IFn)this.src).invoke(c);
        Object object2 = c;
        c = null;
        return Tuple.create((Object)object, (Object)object2);
    }
}

