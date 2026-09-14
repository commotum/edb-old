/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Peer;

public final class api$part
extends AFunction {
    public static Object invokeStatic(Object eid) {
        Object object = eid;
        eid = null;
        return Peer.part(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$part.invokeStatic(object2);
    }
}

