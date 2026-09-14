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
import java.util.concurrent.Future;

public final class memcached$safe_deref
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object fut) {
        Object object;
        if (((Future)fut).isCancelled()) {
            object = null;
        } else {
            Object object2 = fut;
            fut = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object2);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$safe_deref.invokeStatic(object2);
    }
}

