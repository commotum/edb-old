/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Peer;
import java.util.Map;

public final class api$administer_system
extends AFunction {
    public static Object invokeStatic(Object options) {
        Object object = options;
        options = null;
        return Peer.administerSystem((Map)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$administer_system.invokeStatic(object2);
    }
}

