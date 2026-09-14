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

public final class io$bbuf__GT_string
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");

    public static Object invokeStatic(Object bb) {
        Object object = bb;
        bb = null;
        return new String((byte[])((IFn)const__0.getRawRoot()).invoke(object), "UTF-8");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$bbuf__GT_string.invokeStatic(object2);
    }
}

