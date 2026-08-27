/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IBlockingDeref
 *  clojure.lang.IDeref
 *  clojure.lang.IObj
 *  clojure.lang.IPending
 *  clojure.lang.IPersistentMap
 */
package datomic;

import clojure.lang.IBlockingDeref;
import clojure.lang.IDeref;
import clojure.lang.IObj;
import clojure.lang.IPending;
import clojure.lang.IPersistentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class common$pfuture$reify__9233
implements IPending,
IBlockingDeref,
IDeref,
IObj {
    final IPersistentMap __meta;
    Object fut;

    public common$pfuture$reify__9233(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.fut = object;
    }

    public common$pfuture$reify__9233(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new common$pfuture$reify__9233(iPersistentMap, this.fut);
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

    public boolean isRealized() {
        common$pfuture$reify__9233 this_ = null;
        return ((Future)this_.fut).isDone();
    }
}

