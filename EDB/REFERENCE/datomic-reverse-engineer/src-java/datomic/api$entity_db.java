/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Entity;

public final class api$entity_db
extends AFunction {
    public static Object invokeStatic(Object entity2) {
        Object object = entity2;
        entity2 = null;
        return ((Entity)object).db();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$entity_db.invokeStatic(object2);
    }
}

