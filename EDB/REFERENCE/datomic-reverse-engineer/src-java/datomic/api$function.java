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

public final class api$function
extends AFunction {
    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return Peer.function((Map)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$function.invokeStatic(object2);
    }
}

