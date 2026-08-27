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

public final class datafy$fn__17216
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"symbol");

    public static Object invokeStatic(Object c) {
        Object object = c;
        c = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)((Class)object).getName());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$fn__17216.invokeStatic(object2);
    }
}

