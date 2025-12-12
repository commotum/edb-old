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

public final class codec$base_64_literal
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.codec", (String)"decode-64");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"string->bytes");

    public static Object invokeStatic(Object literal) {
        Object object = literal;
        literal = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return codec$base_64_literal.invokeStatic(object2);
    }
}

