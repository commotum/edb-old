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
import datomic.memcached.RecoveringClient;
import java.util.concurrent.Semaphore;

public final class memcached$create_recovering_client
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");

    public static Object invokeStatic(Object create_client2) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(((IFn)create_client2).invoke());
        Object object2 = create_client2;
        create_client2 = null;
        return new RecoveringClient(object, object2, new Semaphore(RT.intCast((long)1L)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$create_recovering_client.invokeStatic(object2);
    }
}

