/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.security.KeyStore;

public final class crypto$load_private_key
extends AFunction {
    public static Object invokeStatic(Object ks, Object alias, Object password) {
        Object object = ks;
        ks = null;
        Object object2 = alias;
        alias = null;
        Object object3 = password;
        password = null;
        return ((KeyStore)object).getKey((String)object2, (char[])object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$load_private_key.invokeStatic(object4, object5, object6);
    }
}

