/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

public final class crypto$crypt_bbuf
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.crypto", (String)"crypt-bbuf");
    public static final Object const__1 = 0L;
    public static final Var const__2 = RT.var((String)"datomic.crypto", (String)"cipher");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__4 = RT.keyword(null, (String)"encrypt");
    public static final Var const__5 = RT.var((String)"datomic.crypto", (String)"random-bytes");
    public static final Keyword const__6 = RT.keyword(null, (String)"decrypt");
    public static final Var const__7 = RT.var((String)"datomic.crypto", (String)"read-n");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object input, Object k, Object op, Object extra) {
        int n;
        Cipher cipher2;
        IvParameterSpec iv;
        Object ivbytes;
        ByteBuffer src;
        int iv_size;
        Object ciph;
        block10: {
            Object object;
            block9: {
                ciph = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)((Key)k).getAlgorithm(), (Object)"/CBC/PKCS5Padding"));
                iv_size = ((Cipher)ciph).getBlockSize();
                Object object2 = input;
                input = null;
                src = ((ByteBuffer)object2).duplicate();
                Object G__23370 = op;
                switch (Util.hash((Object)G__23370) >> 1 & 1) {
                    case 0: {
                        if (G__23370 != const__4) break;
                        object = ((IFn)const__5.getRawRoot()).invoke((Object)iv_size);
                        break block9;
                    }
                    case 1: {
                        if (G__23370 != const__6) break;
                        object = ((IFn)const__7.getRawRoot()).invoke((Object)src, (Object)iv_size);
                        break block9;
                    }
                }
                Object object3 = G__23370;
                G__23370 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__3.getRawRoot()).invoke((Object)"No matching clause: ", object3));
            }
            ivbytes = object;
            iv = new IvParameterSpec((byte[])ivbytes);
            cipher2 = (Cipher)ciph;
            Object G__23371 = op;
            switch (Util.hash((Object)G__23371) >> 1 & 1) {
                case 0: {
                    if (G__23371 != const__4) break;
                    n = Cipher.ENCRYPT_MODE;
                    break block10;
                }
                case 1: {
                    if (G__23371 != const__6) break;
                    n = Cipher.DECRYPT_MODE;
                    break block10;
                }
            }
            Object object4 = G__23371;
            G__23371 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__3.getRawRoot()).invoke((Object)"No matching clause: ", object4));
        }
        Object object = k;
        k = null;
        IvParameterSpec ivParameterSpec = iv;
        iv = null;
        cipher2.init(n, (Key)object, ivParameterSpec);
        int n2 = ((Cipher)ciph).getOutputSize(((Buffer)src).remaining());
        Object object5 = extra;
        extra = null;
        ByteBuffer dest = ByteBuffer.allocate(RT.intCast((Object)Numbers.add((long)Numbers.add((long)iv_size, (long)n2), (Object)object5)));
        Object object6 = op;
        op = null;
        if (Util.equiv((Object)const__4, (Object)object6)) {
            Object object7 = ivbytes;
            ivbytes = null;
            dest.put((byte[])object7);
        }
        Object object8 = ciph;
        ciph = null;
        ByteBuffer byteBuffer = src;
        src = null;
        Integer.valueOf(((Cipher)object8).doFinal(byteBuffer, dest));
        return dest.flip();
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
        return crypto$crypt_bbuf.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object input, Object k, Object op) {
        Object object = input;
        input = null;
        Object object2 = k;
        k = null;
        Object object3 = op;
        op = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, const__1);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$crypt_bbuf.invokeStatic(object4, object5, object6);
    }
}

