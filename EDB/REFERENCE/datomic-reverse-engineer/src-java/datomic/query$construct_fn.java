/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.query$construct_fn$fn__19560;

public final class query$construct_fn
extends AFunction {
    public static Object invokeStatic(Object qform) {
        Object object = qform;
        qform = null;
        return new query$construct_fn$fn__19560(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$construct_fn.invokeStatic(object2);
    }
}

