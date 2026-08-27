/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.datalog$partv$pv__18118$fn__18119;

public final class datalog$partv$pv__18118
extends AFunction {
    long n;

    public datalog$partv$pv__18118(long l) {
        this.n = l;
    }

    public Object invoke(Object iter2) {
        Object object = iter2;
        iter2 = null;
        return new LazySeq((IFn)new datalog$partv$pv__18118$fn__18119(object, (Object)this, this.n));
    }
}

