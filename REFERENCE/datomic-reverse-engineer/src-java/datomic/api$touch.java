/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Entity;

public final class api$touch
extends AFunction {
    public static Object invokeStatic(Object entity2) {
        Object object = entity2;
        entity2 = null;
        return ((Entity)object).touch();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$touch.invokeStatic(object2);
    }
}

