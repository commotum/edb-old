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

public final class tools$flush_progress
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"await");
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"serializer");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
    }

    public Object invoke() {
        return tools$flush_progress.invokeStatic();
    }
}

