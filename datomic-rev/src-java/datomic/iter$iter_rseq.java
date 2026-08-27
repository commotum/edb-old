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
import datomic.iter$iter_rseq$fn__11748;

public final class iter$iter_rseq
extends AFunction {
    public static Object invokeStatic(Object iter2) {
        Object object = iter2;
        iter2 = null;
        return new LazySeq((IFn)new iter$iter_rseq$fn__11748(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return iter$iter_rseq.invokeStatic(object2);
    }
}

