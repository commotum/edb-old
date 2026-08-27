/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.async.DiscardPolicy;

public final class async$discard_policy
extends AFunction {
    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return new DiscardPolicy(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return async$discard_policy.invokeStatic(object2);
    }
}

