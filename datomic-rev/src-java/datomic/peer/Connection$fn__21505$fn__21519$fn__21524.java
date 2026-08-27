/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.peer.Connection$fn__21505$fn__21519$fn__21524$fn__21525;

public final class Connection$fn__21505$fn__21519$fn__21524
extends AFunction {
    Object cleanup;
    Object shutdown;
    Object updater;

    public Connection$fn__21505$fn__21519$fn__21524(Object object, Object object2, Object object3) {
        this.cleanup = object;
        this.shutdown = object2;
        this.updater = object3;
    }

    public Object invoke() {
        ((IFn)new Connection$fn__21505$fn__21519$fn__21524$fn__21525(this_.shutdown, this_.updater)).invoke();
        Connection$fn__21505$fn__21519$fn__21524 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

