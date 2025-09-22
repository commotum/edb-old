/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  datomic.spy.memcached.internal.OperationCompletionListener
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import datomic.spy.memcached.internal.OperationCompletionListener;
import java.util.concurrent.Future;

public final class memcached$op_listener$reify__9944
implements OperationCompletionListener,
IObj {
    final IPersistentMap __meta;
    Object f;

    public memcached$op_listener$reify__9944(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.f = object;
    }

    public memcached$op_listener$reify__9944(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new memcached$op_listener$reify__9944(iPersistentMap, this.f);
    }

    public void onComplete(Future fut) throws Exception {
        Future future2 = fut;
        fut = null;
        memcached$op_listener$reify__9944 this_ = null;
        ((IFn)this_.f).invoke((Object)future2);
    }
}

