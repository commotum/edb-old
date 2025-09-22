/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.core2.algo.lazy$fully_partition_by$fpb__19419$fn__19420;

public final class lazy$fully_partition_by$fpb__19419
extends AFunction {
    public Object invoke(Object f, Object pcoll) {
        Object object = pcoll;
        pcoll = null;
        Object object2 = f;
        f = null;
        return new LazySeq((IFn)new lazy$fully_partition_by$fpb__19419$fn__19420(object, (Object)this, object2));
    }
}

