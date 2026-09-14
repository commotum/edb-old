/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.nio.ByteBuffer;
import java.security.Key;
import javax.crypto.Mac;

public final class crypto$calc_hmac
extends AFunction {
    public static Object invokeStatic(Object bbuf, Object alg, Object k) {
        ByteBuffer result2;
        Object object = alg;
        alg = null;
        Mac G__23366 = Mac.getInstance((String)object);
        Object object2 = k;
        k = null;
        G__23366.init((Key)object2);
        Mac mac2 = null;
        mac2 = G__23366;
        Object object3 = bbuf;
        bbuf = null;
        ByteBuffer byteBuffer = result2 = ((ByteBuffer)object3).duplicate();
        result2 = null;
        mac2.update(byteBuffer);
        Mac mac3 = mac2;
        mac2 = null;
        return mac3.doFinal();
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$calc_hmac.invokeStatic(object4, object5, object6);
    }
}

