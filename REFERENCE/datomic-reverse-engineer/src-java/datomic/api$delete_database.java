/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Peer;

public final class api$delete_database
extends AFunction {
    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        return Peer.deleteDatabase(object) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$delete_database.invokeStatic(object2);
    }
}

