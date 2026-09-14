/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.security.Key;

public final class crypto$ser_key
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"key");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"bytes->string");
    public static final Var const__2 = RT.var((String)"datomic.codec", (String)"encode-64");
    public static final Keyword const__3 = RT.keyword(null, (String)"alg");

    public static Object invokeStatic(Object k) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__0;
        objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)((Key)k).getEncoded()));
        objectArray[2] = const__3;
        Object object = k;
        k = null;
        objectArray[3] = ((Key)object).getAlgorithm();
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$ser_key.invokeStatic(object2);
    }
}

