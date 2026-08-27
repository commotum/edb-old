/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.process$create_instance$fn__14972;
import datomic.process.SharedCriticalFailure;

public final class process$create_instance
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"promise");

    public static Object invokeStatic(Object shutdown_time, Object exit_QMARK_) {
        Object handlers = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
        Object prom = ((IFn)const__1.getRawRoot()).invoke();
        Object object = handlers;
        Object object2 = prom;
        Object object3 = shutdown_time;
        shutdown_time = null;
        Object object4 = handlers;
        handlers = null;
        Object object5 = exit_QMARK_;
        exit_QMARK_ = null;
        Object object6 = prom;
        prom = null;
        return new SharedCriticalFailure(object, object2, new Delay((IFn)new process$create_instance$fn__14972(object3, object4, object5, object6)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return process$create_instance.invokeStatic(object3, object4);
    }
}

