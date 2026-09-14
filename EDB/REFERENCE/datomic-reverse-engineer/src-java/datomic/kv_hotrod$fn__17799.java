/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.Properties;

public final class kv_hotrod$fn__17799
extends AFunction {
    public static Object invokeStatic() {
        Properties G__17798 = new Properties();
        G__17798.setProperty("infinispan.client.hotrod.key_size_estimate", "128");
        G__17798.setProperty("infinispan.client.hotrod.value_size_estimate", "64000");
        G__17798.setProperty("infinispan.client.hotrod.socket_timeout", "10000");
        G__17798.setProperty("infinispan.client.hotrod.connect_timeout", "10000");
        Object var0 = null;
        return G__17798;
    }

    public Object invoke() {
        return kv_hotrod$fn__17799.invokeStatic();
    }
}

