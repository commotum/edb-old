/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Peer;

public final class api$rename_database
extends AFunction {
    public static Object invokeStatic(Object uri2, Object new_name) {
        Object object = uri2;
        uri2 = null;
        Object object2 = new_name;
        new_name = null;
        return Peer.renameDatabase(object, (String)object2) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return api$rename_database.invokeStatic(object3, object4);
    }
}

