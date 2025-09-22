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

public final class api$tx__GT_t
extends AFunction
implements IFn.OL {
    public static long invokeStatic(Object tx) {
        Object object = tx;
        tx = null;
        return Peer.toT(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return new Long(api$tx__GT_t.invokeStatic(object2));
    }

    public final long invokePrim(Object object) {
        Object object2 = object;
        object = null;
        return api$tx__GT_t.invokeStatic(object2);
    }
}

