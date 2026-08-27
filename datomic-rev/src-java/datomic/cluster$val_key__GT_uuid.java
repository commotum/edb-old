/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.UUID;

public final class cluster$val_key__GT_uuid
extends AFunction {
    public static Object invokeStatic(Object val_key) {
        Object object = val_key;
        val_key = null;
        return UUID.fromString((String)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$val_key__GT_uuid.invokeStatic(object2);
    }
}

