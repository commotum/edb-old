/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Peer;

public final class api$shutdown
extends AFunction {
    public static Object invokeStatic(Object shutdown_clojure) {
        Object object = shutdown_clojure;
        shutdown_clojure = null;
        Peer.shutdown((Boolean)object);
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$shutdown.invokeStatic(object2);
    }
}

