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

public final class valcache$start_server$accept_loop__9804$fn__9805
extends AFunction {
    Object sc;
    Object socket_loop;

    public valcache$start_server$accept_loop__9804$fn__9805(Object object, Object object2) {
        this.sc = object;
        this.socket_loop = object2;
    }

    public Object invoke() {
        valcache$start_server$accept_loop__9804$fn__9805 this_ = null;
        return ((IFn)this_.socket_loop).invoke(this_.sc);
    }
}

