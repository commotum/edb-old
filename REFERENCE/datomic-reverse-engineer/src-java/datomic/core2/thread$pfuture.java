/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.thread$pfuture$reify__21007;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class thread$pfuture
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.thread", (String)"binding-conveyor-fn");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 30, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object f, Object exec) {
        Future fut;
        Object object = exec;
        exec = null;
        Object object2 = f;
        f = null;
        Future future2 = fut = ((ExecutorService)object).submit((Callable)((IFn)const__0.getRawRoot()).invoke(object2));
        fut = null;
        return ((IObj)new thread$pfuture$reify__21007(null, future2)).withMeta((IPersistentMap)const__5);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$pfuture.invokeStatic(object3, object4);
    }
}

