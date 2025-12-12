/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.artemis_client$create_deserializer$fn__20863;

public final class artemis_client$create_deserializer
extends AFunction {
    public static Object invokeStatic(Object read_handlers) {
        Object object = read_handlers;
        read_handlers = null;
        return new artemis_client$create_deserializer$fn__20863(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return artemis_client$create_deserializer.invokeStatic(object2);
    }
}

