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
import java.security.PrivateKey;
import java.security.Signature;

public final class crypto$sign
extends AFunction {
    public static Object invokeStatic(Object algo, Object priv, Object arr) {
        Object object = algo;
        algo = null;
        Signature signer = Signature.getInstance((String)object);
        Object object2 = priv;
        priv = null;
        signer.initSign((PrivateKey)object2);
        byte[] byArray = (byte[])arr;
        Object object3 = arr;
        arr = null;
        signer.update(byArray, RT.intCast((long)0L), RT.count((Object)object3));
        Signature signature = signer;
        signer = null;
        return signature.sign();
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$sign.invokeStatic(object4, object5, object6);
    }
}

