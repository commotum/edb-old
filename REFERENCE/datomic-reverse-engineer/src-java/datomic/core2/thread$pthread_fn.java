/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.Executor;

public final class thread$pthread_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.thread", (String)"binding-conveyor-fn");

    public static Object invokeStatic(Object exec, Object fn2) {
        Object object = exec;
        exec = null;
        Object object2 = fn2;
        fn2 = null;
        ((Executor)object).execute((Runnable)((IFn)const__0.getRawRoot()).invoke(object2));
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$pthread_fn.invokeStatic(object3, object4);
    }
}

