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
import java.security.Key;
import javax.crypto.Cipher;

public final class crypto$encrypt_pk
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.crypto", (String)"cipher");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"string->bytes");

    public static Object invokeStatic(Object plaintext, Object key) {
        Object c = ((IFn)const__0.getRawRoot()).invoke((Object)((Key)key).getAlgorithm());
        Object object = key;
        key = null;
        ((Cipher)c).init(Cipher.ENCRYPT_MODE, (Key)object);
        Object object2 = c;
        c = null;
        Object object3 = plaintext;
        plaintext = null;
        return ((Cipher)object2).doFinal((byte[])((IFn)const__1.getRawRoot()).invoke(object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$encrypt_pk.invokeStatic(object3, object4);
    }
}

