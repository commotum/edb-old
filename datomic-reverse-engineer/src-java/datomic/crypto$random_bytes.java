/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.security.SecureRandom;

public final class crypto$random_bytes
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.crypto", (String)"random");

    public static Object invokeStatic(Object n) {
        Object object = n;
        n = null;
        byte[] dest = Numbers.byte_array((Object)object);
        ((SecureRandom)const__1.getRawRoot()).nextBytes(dest);
        Object var1_1 = null;
        return dest;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$random_bytes.invokeStatic(object2);
    }
}

