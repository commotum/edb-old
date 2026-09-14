/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class crypto$append_hmac
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.crypto", (String)"hmac-length");
    public static final Var const__3 = RT.var((String)"datomic.crypto", (String)"calc-hmac");

    public static Object invokeStatic(Object bbuf, Object alg, Object k) {
        ByteBuffer result2 = ((ByteBuffer)bbuf).duplicate();
        result2.position(((Buffer)result2).limit());
        result2.limit(RT.intCast((Object)Numbers.add((long)((Buffer)result2).limit(), (Object)((IFn)const__2.getRawRoot()).invoke(alg))));
        Object object = bbuf;
        bbuf = null;
        Object object2 = alg;
        alg = null;
        Object object3 = k;
        k = null;
        result2.put((byte[])((IFn)const__3.getRawRoot()).invoke(object, object2, object3));
        ByteBuffer byteBuffer = result2;
        result2 = null;
        return byteBuffer.flip();
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$append_hmac.invokeStatic(object4, object5, object6);
    }
}

