/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.common$distinct_by$step__9247$fn__9248$fn__9250;

public final class common$distinct_by$step__9247$fn__9248
extends AFunction {
    Object f;
    Object seen;
    Object xs;
    Object step;

    public common$distinct_by$step__9247$fn__9248(Object object, Object object2, Object object3, Object object4) {
        this.f = object;
        this.seen = object2;
        this.xs = object3;
        this.step = object4;
    }

    public Object invoke() {
        this_.f = null;
        this_.xs = null;
        this_.seen = null;
        common$distinct_by$step__9247$fn__9248 this_ = null;
        return ((IFn)new common$distinct_by$step__9247$fn__9248$fn__9250(this_.f, this_.step)).invoke(this_.xs, this_.seen);
    }
}

