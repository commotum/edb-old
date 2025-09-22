/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.security.KeyStore;

public final class crypto$load_public_key
extends AFunction {
    public static Object invokeStatic(Object ks, Object alias) {
        Object object = ks;
        ks = null;
        Object object2 = alias;
        alias = null;
        return ((KeyStore)object).getCertificate((String)object2).getPublicKey();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$load_public_key.invokeStatic(object3, object4);
    }
}

