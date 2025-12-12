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
import datomic.tools$cause_chain$fn__21807;

public final class tools$cause_chain
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"iterate");

    public static Object invokeStatic(Object t) {
        Object object = t;
        t = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke((Object)new tools$cause_chain$fn__21807(), object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$cause_chain.invokeStatic(object2);
    }
}

