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
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class crypto$decrypt
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.crypto", (String)"cipher");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"bytes->string");

    public static Object invokeStatic(Object ciphertext, Object cipher_name, Object rawkey) {
        Object c = ((IFn)const__0.getRawRoot()).invoke(cipher_name);
        Object object = rawkey;
        rawkey = null;
        Object object2 = cipher_name;
        cipher_name = null;
        ((Cipher)c).init(Cipher.DECRYPT_MODE, new SecretKeySpec((byte[])object, (String)object2));
        Object object3 = c;
        c = null;
        Object object4 = ciphertext;
        ciphertext = null;
        return ((IFn)const__1.getRawRoot()).invoke((Object)((Cipher)object3).doFinal((byte[])object4));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$decrypt.invokeStatic(object4, object5, object6);
    }
}

