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
import datomic.peer.Connection$fn__21505$fn__21519$fn__21520$fn__21521;

public final class Connection$fn__21505$fn__21519$fn__21520
extends AFunction {
    Object cleanup;
    Object _;

    public Connection$fn__21505$fn__21519$fn__21520(Object object, Object object2) {
        this.cleanup = object;
        this._ = object2;
    }

    public Object invoke() {
        ((IFn)new Connection$fn__21505$fn__21519$fn__21520$fn__21521(this_._)).invoke();
        Connection$fn__21505$fn__21519$fn__21520 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

