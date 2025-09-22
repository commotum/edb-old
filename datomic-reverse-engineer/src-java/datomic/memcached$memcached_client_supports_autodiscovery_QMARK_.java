/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class memcached$memcached_client_supports_autodiscovery_QMARK_
extends AFunction {
    public static Object invokeStatic() {
        Class<?> clazz;
        try {
            clazz = Class.forName("datomic.spy.memcached.ClientMode");
        }
        catch (ClassNotFoundException _) {
            clazz = null;
        }
        return clazz;
    }

    public Object invoke() {
        return memcached$memcached_client_supports_autodiscovery_QMARK_.invokeStatic();
    }
}

