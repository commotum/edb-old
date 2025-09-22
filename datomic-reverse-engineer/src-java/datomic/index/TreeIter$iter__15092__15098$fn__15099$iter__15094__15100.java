/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.index.TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101;

public final class TreeIter$iter__15092__15098$fn__15099$iter__15094__15100
extends AFunction {
    Object d;
    Object ri;
    int ridx;
    int didx;

    public TreeIter$iter__15092__15098$fn__15099$iter__15094__15100(Object object, Object object2, int n, int n2) {
        this.d = object;
        this.ri = object2;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke(Object s__15095) {
        Object object = s__15095;
        s__15095 = null;
        return new LazySeq((IFn)new TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101(this.d, (Object)this, this.ri, object, this.ridx, this.didx));
    }
}

