/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.Peer;

public final class api$tempid
extends AFunction {
    public static Object invokeStatic(Object partition, Object n) {
        Object object = partition;
        partition = null;
        Object object2 = n;
        n = null;
        return Peer.tempid(object, RT.longCast((Object)((Number)object2)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return api$tempid.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object partition) {
        Object object = partition;
        partition = null;
        return Peer.tempid(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$tempid.invokeStatic(object2);
    }
}

