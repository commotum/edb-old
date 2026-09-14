/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

public final class crypto$spec__GT_public_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.codec", (String)"decode-64");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"string->bytes");

    public static Object invokeStatic(Object algo, Object b64encoded) {
        Object object = algo;
        algo = null;
        KeyFactory kf = KeyFactory.getInstance((String)object);
        Object object2 = b64encoded;
        b64encoded = null;
        Object encoded = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object2));
        KeyFactory keyFactory = kf;
        kf = null;
        Object object3 = encoded;
        encoded = null;
        return keyFactory.generatePublic(new X509EncodedKeySpec((byte[])object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$spec__GT_public_key.invokeStatic(object3, object4);
    }
}

