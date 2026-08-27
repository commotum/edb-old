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
import datomic.index.TreeIter$iter__15092__15098$fn__15099;

public final class TreeIter$iter__15092__15098
extends AFunction {
    Object lookup;
    Object root;
    int ridx;
    int didx;

    public TreeIter$iter__15092__15098(Object object, Object object2, int n, int n2) {
        this.lookup = object;
        this.root = object2;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke(Object s__15093) {
        Object object = s__15093;
        s__15093 = null;
        return new LazySeq((IFn)new TreeIter$iter__15092__15098$fn__15099(object, this.lookup, this.root, (Object)this, this.ridx, this.didx));
    }
}

