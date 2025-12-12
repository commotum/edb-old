/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import java.security.Key;
import javax.crypto.KeyGenerator;

public final class crypto$genkey
extends AFunction {
    public static Object invokeStatic(Object cipher_name) {
        Object object = cipher_name;
        cipher_name = null;
        KeyGenerator keygen = KeyGenerator.getInstance((String)object);
        keygen.init(RT.intCast((long)128L));
        KeyGenerator keyGenerator = keygen;
        keygen = null;
        return ((Key)keyGenerator.generateKey()).getEncoded();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$genkey.invokeStatic(object2);
    }
}

