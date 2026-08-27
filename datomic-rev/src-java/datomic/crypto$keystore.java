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
import java.io.InputStream;
import java.security.KeyStore;

public final class crypto$keystore
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.crypto", (String)"keystore");
    public static final Var const__1 = RT.var((String)"clojure.java.io", (String)"input-stream");

    public static Object invokeStatic(Object f, Object password, Object type) {
        KeyStore keyStore;
        Object object = f;
        f = null;
        Object is = ((IFn)const__1.getRawRoot()).invoke(object);
        try {
            Object object2 = type;
            type = null;
            KeyStore G__23375 = KeyStore.getInstance((String)object2);
            Object object3 = password;
            password = null;
            G__23375.load((InputStream)is, (char[])object3);
            KeyStore keyStore2 = G__23375;
            G__23375 = null;
            keyStore = keyStore2;
        }
        finally {
            Object object4 = is;
            is = null;
            ((InputStream)object4).close();
        }
        return keyStore;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return crypto$keystore.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object f, Object password) {
        Object object = f;
        f = null;
        Object object2 = password;
        password = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)KeyStore.getDefaultType());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$keystore.invokeStatic(object3, object4);
    }
}

