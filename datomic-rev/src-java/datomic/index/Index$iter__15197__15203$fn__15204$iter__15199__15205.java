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
import datomic.index.Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206;

public final class Index$iter__15197__15203$fn__15204$iter__15199__15205
extends AFunction {
    Object d;

    public Index$iter__15197__15203$fn__15204$iter__15199__15205(Object object) {
        this.d = object;
    }

    public Object invoke(Object s__15200) {
        Object object = s__15200;
        s__15200 = null;
        return new LazySeq((IFn)new Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206(object, this.d, (Object)this));
    }
}

