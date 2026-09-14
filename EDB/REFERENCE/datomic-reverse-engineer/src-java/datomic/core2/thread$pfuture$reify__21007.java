/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IBlockingDeref
 *  clojure.lang.IDeref
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic.core2;

import clojure.lang.IBlockingDeref;
import clojure.lang.IDeref;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class thread$pfuture$reify__21007
implements IBlockingDeref,
IDeref,
IObj {
    final IPersistentMap __meta;
    Object fut;

    public thread$pfuture$reify__21007(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.fut = object;
    }

    public thread$pfuture$reify__21007(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new thread$pfuture$reify__21007(iPersistentMap, this.fut);
    }

    public Object deref(long timeout_ms, Object object) {
        Object object2;
        try {
            object2 = ((Future)this.fut).get(timeout_ms, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e) {
            Object object3 = object;
            object = null;
            object2 = object3;
        }
        return object2;
    }

    public Object deref() {
        return ((Future)this.fut).get();
    }
}

