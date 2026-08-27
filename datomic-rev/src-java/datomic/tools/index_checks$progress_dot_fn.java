/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.tools.index_checks$progress_dot_fn$fn__21922;

public final class index_checks$progress_dot_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__1 = 0L;

    public static Object invokeStatic(Object n) {
        Object c = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object object = n;
        n = null;
        Object object2 = c;
        c = null;
        return new index_checks$progress_dot_fn$fn__21922(object, object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index_checks$progress_dot_fn.invokeStatic(object2);
    }
}

