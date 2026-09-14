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
import java.security.PublicKey;
import java.security.Signature;

public final class crypto$verify
extends AFunction {
    public static Object invokeStatic(Object algo, Object pub, Object arr, Object sig) {
        Object object = algo;
        algo = null;
        Signature verifier = Signature.getInstance((String)object);
        Object object2 = pub;
        pub = null;
        verifier.initVerify((PublicKey)object2);
        byte[] byArray = (byte[])arr;
        Object object3 = arr;
        arr = null;
        verifier.update(byArray, RT.intCast((long)0L), RT.count((Object)object3));
        Signature signature = verifier;
        verifier = null;
        Object object4 = sig;
        sig = null;
        return signature.verify((byte[])object4) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return crypto$verify.invokeStatic(object5, object6, object7, object8);
    }
}

