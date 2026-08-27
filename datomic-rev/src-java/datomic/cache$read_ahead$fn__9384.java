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
import datomic.cache$read_ahead$fn__9384$fn__9385;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class cache$read_ahead$fn__9384
extends AFunction {
    Object lookup;
    Object k;
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"get-from-cache");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"bound-fn*");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"datomic.cache", (String)"read-ahead-pool");

    public cache$read_ahead$fn__9384(Object object, Object object2) {
        this.lookup = object;
        this.k = object2;
    }

    public Object invoke() {
        Future future2;
        Object object = ((IFn)const__0.getRawRoot()).invoke(this.lookup, this.k, null);
        if (object != null && object != Boolean.FALSE) {
            future2 = null;
        } else {
            Object f;
            Object object2 = f = ((IFn)const__1.getRawRoot()).invoke((Object)new cache$read_ahead$fn__9384$fn__9385(this.lookup, this.k));
            f = null;
            future2 = ((ExecutorService)((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot())).submit((Callable)object2);
        }
        return future2;
    }
}

