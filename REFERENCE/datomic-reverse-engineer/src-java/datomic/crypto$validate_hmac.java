/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class crypto$validate_hmac
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.crypto", (String)"hmac-length");
    public static final Var const__2 = RT.var((String)"datomic.io", (String)"valid-buf-limit?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"db", (String)"error"), RT.keyword((String)"datomic.crypto", (String)"hmac-missing")});
    public static final Var const__9 = RT.var((String)"datomic.crypto", (String)"calc-hmac");
    public static final AFn const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"db", (String)"error"), RT.keyword((String)"datomic.crypto", (String)"validate-hmac-failed")});

    public static Object invokeStatic(Object bbuf, Object alg, Object k) {
        ByteBuffer input;
        Number hmac_offset = Numbers.minus((long)((Buffer)bbuf).limit(), (Object)((IFn)const__1.getRawRoot()).invoke(alg));
        Object object = ((IFn)const__2.getRawRoot()).invoke(bbuf, (Object)hmac_offset);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((IFn)const__3.getRawRoot()).invoke((Object)"HMAC integrity check failed", (Object)const__6);
        }
        ByteBuffer result2 = ((ByteBuffer)bbuf).duplicate().limit(RT.intCast((Object)hmac_offset));
        byte[] buf_hmac = Numbers.byte_array((Object)((IFn)const__1.getRawRoot()).invoke(alg));
        Object object2 = bbuf;
        bbuf = null;
        Number number = hmac_offset;
        hmac_offset = null;
        ByteBuffer byteBuffer = input = ((ByteBuffer)object2).duplicate().position(RT.intCast((Object)number));
        input = null;
        byteBuffer.get(buf_hmac);
        byte[] byArray = buf_hmac;
        buf_hmac = null;
        Object object3 = alg;
        alg = null;
        Object object4 = k;
        k = null;
        if (!Arrays.equals(byArray, (byte[])((IFn)const__9.getRawRoot()).invoke((Object)result2, object3, object4))) {
            throw (Throwable)((IFn)const__3.getRawRoot()).invoke((Object)"HMAC integrity check failed", (Object)const__11);
        }
        ByteBuffer byteBuffer2 = result2;
        result2 = null;
        return byteBuffer2;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$validate_hmac.invokeStatic(object4, object5, object6);
    }
}

