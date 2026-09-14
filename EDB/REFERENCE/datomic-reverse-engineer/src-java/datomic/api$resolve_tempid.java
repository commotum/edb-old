/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;
import datomic.Peer;

public final class api$resolve_tempid
extends AFunction {
    public static Object invokeStatic(Object db2, Object tempids, Object tempid2) {
        Object object = db2;
        db2 = null;
        Object object2 = tempids;
        tempids = null;
        Object object3 = tempid2;
        tempid2 = null;
        return Peer.resolveTempid((Database)object, object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return api$resolve_tempid.invokeStatic(object4, object5, object6);
    }
}

