/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.valcache.Server;

public final class valcache$fn__9758$__GT_Server__9760
extends AFunction {
    public Object invoke(Object sem, Object concurrency, Object socket_registry, Object handled, Object host, Object port, Object path2, Object shutdown_fn) {
        Object object = sem;
        sem = null;
        Object object2 = concurrency;
        concurrency = null;
        Object object3 = socket_registry;
        socket_registry = null;
        Object object4 = handled;
        handled = null;
        Object object5 = host;
        host = null;
        Object object6 = port;
        port = null;
        Object object7 = path2;
        path2 = null;
        Object object8 = shutdown_fn;
        shutdown_fn = null;
        return new Server(object, RT.longCast((Object)((Number)object2)), object3, object4, object5, object6, object7, object8);
    }
}

