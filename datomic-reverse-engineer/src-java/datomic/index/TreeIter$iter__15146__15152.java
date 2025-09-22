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
import datomic.index.TreeIter$iter__15146__15152$fn__15153;

public final class TreeIter$iter__15146__15152
extends AFunction {
    Object lookup;
    Object root;
    int ridx;
    int didx;

    public TreeIter$iter__15146__15152(Object object, Object object2, int n, int n2) {
        this.lookup = object;
        this.root = object2;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke(Object s__15147) {
        Object object = s__15147;
        s__15147 = null;
        return new LazySeq((IFn)new TreeIter$iter__15146__15152$fn__15153(this.lookup, this.root, (Object)this, object, this.ridx, this.didx));
    }
}

