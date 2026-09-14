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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class future$_future_with_channel_impl
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.future", (String)"filling-promise");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__5 = RT.var((String)"datomic.future", (String)"->ClojureFutureWithChannel");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"binding-conveyor-fn");
    public static final Var const__8 = RT.var((String)"datomic.future", (String)"->JavaFutureWithChannel");

    public static Object invokeStatic(Object exec, Object f) {
        Future fut;
        Object object = f;
        f = null;
        Object vec__10177 = ((IFn)const__0.getRawRoot()).invoke(((IFn)((IFn)const__6.getRawRoot()).invoke((Object)const__7)).invoke(object));
        Object f2 = RT.nth((Object)vec__10177, (int)RT.intCast((long)0L), null);
        Object object2 = vec__10177;
        vec__10177 = null;
        Object ch = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = exec;
        exec = null;
        Object object4 = f2;
        f2 = null;
        Future future2 = fut = ((ExecutorService)object3).submit((Callable)object4);
        fut = null;
        Object object5 = ch;
        ch = null;
        return ((IFn)const__8.getRawRoot()).invoke(future2, object5);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return future$_future_with_channel_impl.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object f) {
        Object fut;
        Object object = f;
        f = null;
        Object vec__10174 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object f2 = RT.nth((Object)vec__10174, (int)RT.intCast((long)0L), null);
        Object object2 = vec__10174;
        vec__10174 = null;
        Object ch = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = f2;
        f2 = null;
        Object object4 = fut = ((IFn)const__4.getRawRoot()).invoke(object3);
        fut = null;
        Object object5 = ch;
        ch = null;
        return ((IFn)const__5.getRawRoot()).invoke(object4, object5);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return future$_future_with_channel_impl.invokeStatic(object2);
    }
}

