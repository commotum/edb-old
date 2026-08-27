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
import datomic.assert$local_bindings$fn__20649;

public final class assert$local_bindings
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"key");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"zipmap");

    public static Object invokeStatic(Object env) {
        Object object = env;
        env = null;
        Object symbols = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object);
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)new assert$local_bindings$fn__20649(), symbols);
        Object object3 = symbols;
        symbols = null;
        return ((IFn)const__2.getRawRoot()).invoke(object2, object3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return assert$local_bindings.invokeStatic(object2);
    }
}

