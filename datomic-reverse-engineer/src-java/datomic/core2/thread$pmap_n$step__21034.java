/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.core2.thread$pmap_n$step__21034$fn__21035;

public final class thread$pmap_n$step__21034
extends AFunction {
    public Object invoke(Object cs) {
        Object object = cs;
        cs = null;
        return new LazySeq((IFn)new thread$pmap_n$step__21034$fn__21035(object, (Object)this));
    }
}

