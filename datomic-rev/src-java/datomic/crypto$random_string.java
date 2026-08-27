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
import java.security.SecureRandom;

public final class crypto$random_string
extends AFunction {
    public static final Var const__5 = RT.var((String)"datomic.codec", (String)"bytes->string");
    public static final Var const__6 = RT.var((String)"datomic.codec", (String)"encode-64");

    public static Object invokeStatic(Object entropy) {
        SecureRandom sr = new SecureRandom();
        Object object = entropy;
        entropy = null;
        byte[] b = Numbers.byte_array((Object)Numbers.quotient((Object)Numbers.add((long)7L, (Object)object), (long)8L));
        SecureRandom secureRandom = sr;
        sr = null;
        secureRandom.nextBytes(b);
        byte[] byArray = b;
        b = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)byArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$random_string.invokeStatic(object2);
    }
}

