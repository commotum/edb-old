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
import datomic.index.TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155;

public final class TreeIter$iter__15146__15152$fn__15153$iter__15148__15154
extends AFunction {
    Object ri;
    Object d;
    int ridx;
    int didx;

    public TreeIter$iter__15146__15152$fn__15153$iter__15148__15154(Object object, Object object2, int n, int n2) {
        this.ri = object;
        this.d = object2;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke(Object s__15149) {
        Object object = s__15149;
        s__15149 = null;
        return new LazySeq((IFn)new TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155((Object)this, object, this.ri, this.d, this.ridx, this.didx));
    }
}

