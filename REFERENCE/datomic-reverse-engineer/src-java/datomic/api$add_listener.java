/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.ListenableFuture;
import java.util.concurrent.Executor;

public final class api$add_listener
extends AFunction {
    public static Object invokeStatic(Object fut, Object f, Object executor) {
        Object object = fut;
        fut = null;
        Object object2 = f;
        f = null;
        Object object3 = executor;
        executor = null;
        ((ListenableFuture)object).addListener((Runnable)object2, (Executor)object3);
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return api$add_listener.invokeStatic(object4, object5, object6);
    }
}

