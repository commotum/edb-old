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
import datomic.common$distinct_by$step__9247$fn__9248;

public final class common$distinct_by$step__9247
extends AFunction {
    Object f;

    public common$distinct_by$step__9247(Object object) {
        this.f = object;
    }

    public Object invoke(Object xs, Object seen) {
        Object object = seen;
        seen = null;
        Object object2 = xs;
        xs = null;
        return new LazySeq((IFn)new common$distinct_by$step__9247$fn__9248(this.f, object, object2, (Object)this));
    }
}

