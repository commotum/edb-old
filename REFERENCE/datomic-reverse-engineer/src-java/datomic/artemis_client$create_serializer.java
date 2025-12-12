/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.artemis_client$create_serializer$fn__20860;

public final class artemis_client$create_serializer
extends AFunction {
    public static Object invokeStatic(Object write_handlers2) {
        Object object = write_handlers2;
        write_handlers2 = null;
        return new artemis_client$create_serializer$fn__20860(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return artemis_client$create_serializer.invokeStatic(object2);
    }
}

