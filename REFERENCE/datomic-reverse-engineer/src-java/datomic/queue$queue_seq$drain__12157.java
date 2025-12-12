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
import datomic.queue$queue_seq$drain__12157$fn__12158;

public final class queue$queue_seq$drain__12157
extends AFunction {
    Object q;

    public queue$queue_seq$drain__12157(Object object) {
        this.q = object;
    }

    public Object invoke() {
        return new LazySeq((IFn)new queue$queue_seq$drain__12157$fn__12158(this.q, (Object)this));
    }
}

