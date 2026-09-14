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
import datomic.peer.Connection$fn__21505$fn__21515$fn__21516;

public final class Connection$fn__21505$fn__21515
extends AFunction {
    Object shutdown;
    Object notifier;

    public Connection$fn__21505$fn__21515(Object object, Object object2) {
        this.shutdown = object;
        this.notifier = object2;
    }

    public Object invoke() {
        ((IFn)new Connection$fn__21505$fn__21515$fn__21516(this.shutdown, this.notifier)).invoke();
        return null;
    }
}

