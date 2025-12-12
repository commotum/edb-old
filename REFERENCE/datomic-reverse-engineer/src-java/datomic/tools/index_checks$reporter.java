/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.tools;

import clojure.lang.AFunction;
import datomic.tools.index_checks$reporter$fn__21902;

public final class index_checks$reporter
extends AFunction {
    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return new index_checks$reporter$fn__21902(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index_checks$reporter.invokeStatic(object2);
    }
}

