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
import datomic.index.TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130;

public final class TreeIter$iter__15121__15127$fn__15128$iter__15123__15129
extends AFunction {
    Object ri;
    Object lookup;
    Object d;
    int ridx;
    int didx;

    public TreeIter$iter__15121__15127$fn__15128$iter__15123__15129(Object object, Object object2, Object object3, int n, int n2) {
        this.ri = object;
        this.lookup = object2;
        this.d = object3;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke(Object s__15124) {
        Object object = s__15124;
        s__15124 = null;
        return new LazySeq((IFn)new TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130(object, this.ri, this.lookup, this.d, (Object)this, this.ridx, this.didx));
    }
}

