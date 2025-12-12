/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OL
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.Peer;
import java.util.UUID;

public final class api$squuid_time_millis
extends AFunction
implements IFn.OL {
    public static long invokeStatic(Object squuid2) {
        Object object = squuid2;
        squuid2 = null;
        return Peer.squuidTimeMillis((UUID)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return new Long(api$squuid_time_millis.invokeStatic(object2));
    }

    public final long invokePrim(Object object) {
        Object object2 = object;
        object = null;
        return api$squuid_time_millis.invokeStatic(object2);
    }
}

