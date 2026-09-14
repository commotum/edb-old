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
import datomic.index.Index$iter__15197__15203$fn__15204;

public final class Index$iter__15197__15203
extends AFunction {
    Object root;
    Object lookup;

    public Index$iter__15197__15203(Object object, Object object2) {
        this.root = object;
        this.lookup = object2;
    }

    public Object invoke(Object s__15198) {
        Object object = s__15198;
        s__15198 = null;
        return new LazySeq((IFn)new Index$iter__15197__15203$fn__15204(this.root, this.lookup, object, (Object)this));
    }
}

