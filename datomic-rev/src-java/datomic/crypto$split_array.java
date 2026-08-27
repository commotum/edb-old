/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class crypto$split_array
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.crypto", (String)"random-bytes");
    public static final Var const__2 = RT.var((String)"datomic.crypto", (String)"xor-arrays");

    public static Object invokeStatic(Object b1) {
        Object b2;
        Object object = b2 = ((IFn)const__0.getRawRoot()).invoke((Object)RT.count((Object)b1));
        Object object2 = b1;
        b1 = null;
        Object object3 = b2;
        b2 = null;
        return Tuple.create((Object)object, (Object)((IFn)const__2.getRawRoot()).invoke(object2, object3));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$split_array.invokeStatic(object2);
    }
}

