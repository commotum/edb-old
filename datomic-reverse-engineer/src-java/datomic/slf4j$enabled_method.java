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

public final class slf4j$enabled_method
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"capitalize");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object level) {
        Object object = level;
        level = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)".is", ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object)), (Object)"Enabled"));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return slf4j$enabled_method.invokeStatic(object2);
    }
}

