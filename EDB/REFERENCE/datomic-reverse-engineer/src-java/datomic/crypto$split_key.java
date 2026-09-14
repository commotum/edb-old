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
import datomic.crypto$split_key$fn__23356;
import java.security.Key;

public final class crypto$split_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"datomic.crypto", (String)"split-array");

    public static Object invokeStatic(Object k) {
        crypto$split_key$fn__23356 crypto$split_key$fn__23356 = new crypto$split_key$fn__23356(k);
        Object object = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)crypto$split_key$fn__23356, ((IFn)const__1.getRawRoot()).invoke((Object)((Key)object).getEncoded()));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$split_key.invokeStatic(object2);
    }
}

