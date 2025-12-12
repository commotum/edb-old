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

public final class io$bbuf__GT_clj
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"bbuf->string");

    public static Object invokeStatic(Object bbuf) {
        Object object = bbuf;
        bbuf = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$bbuf__GT_clj.invokeStatic(object2);
    }
}

