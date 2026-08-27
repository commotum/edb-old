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

public final class codec$encode_64
extends AFunction {
    public static Object invokeStatic(Object raw) {
        Object object = raw;
        raw = null;
        return Base64.encodeBase64((byte[])((byte[])object), (boolean)Boolean.FALSE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return codec$encode_64.invokeStatic(object2);
    }
}

