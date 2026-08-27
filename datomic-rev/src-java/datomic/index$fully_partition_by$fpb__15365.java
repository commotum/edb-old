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
import datomic.index$fully_partition_by$fpb__15365$fn__15366;

public final class index$fully_partition_by$fpb__15365
extends AFunction {
    public Object invoke(Object f, Object pcoll) {
        Object object = pcoll;
        pcoll = null;
        Object object2 = f;
        f = null;
        return new LazySeq((IFn)new index$fully_partition_by$fpb__15365$fn__15366(object, (Object)this, object2));
    }
}

