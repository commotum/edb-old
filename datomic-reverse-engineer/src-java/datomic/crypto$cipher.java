/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import javax.crypto.Cipher;

public final class crypto$cipher
extends AFunction {
    public static Object invokeStatic(Object name) {
        Object object = name;
        name = null;
        return Cipher.getInstance((String)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$cipher.invokeStatic(object2);
    }
}

