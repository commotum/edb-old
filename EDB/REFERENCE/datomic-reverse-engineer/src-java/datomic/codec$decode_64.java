/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.apache.commons.codec.binary.Base64
 */
package datomic;

import clojure.lang.AFunction;
import org.apache.commons.codec.binary.Base64;

public final class codec$decode_64
extends AFunction {
    public static Object invokeStatic(Object coded) {
        Object object = coded;
        coded = null;
        return Base64.decodeBase64((byte[])((byte[])object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return codec$decode_64.invokeStatic(object2);
    }
}

