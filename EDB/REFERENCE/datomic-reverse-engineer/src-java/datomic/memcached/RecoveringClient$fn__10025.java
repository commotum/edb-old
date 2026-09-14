/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  datomic.spy.memcached.MemcachedClient
 */
package datomic.memcached;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.spy.memcached.MemcachedClient;
import java.util.concurrent.Semaphore;

public final class RecoveringClient$fn__10025
extends AFunction {
    Object create_client;
    Object client_ref;
    Object sem;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reset!");

    public RecoveringClient$fn__10025(Object object, Object object2, Object object3) {
        this.create_client = object;
        this.client_ref = object2;
        this.sem = object3;
    }

    public Object invoke() {
        Object var2_2;
        try {
            Object old = ((IFn)const__0.getRawRoot()).invoke(this.client_ref);
            ((IFn)const__1.getRawRoot()).invoke(this.client_ref, ((IFn)this.create_client).invoke());
            Object object = old;
            old = null;
            ((MemcachedClient)object).shutdown();
            var2_2 = null;
        }
        finally {
            ((Semaphore)this.sem).release();
        }
        return var2_2;
    }
}

