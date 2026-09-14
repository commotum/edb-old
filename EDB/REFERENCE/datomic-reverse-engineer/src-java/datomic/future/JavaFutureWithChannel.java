/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IBlockingDeref
 *  clojure.lang.IDeref
 *  clojure.lang.IObj
 *  clojure.lang.IPending
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.future;

import clojure.lang.IBlockingDeref;
import clojure.lang.IDeref;
import clojure.lang.IObj;
import clojure.lang.IPending;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.future.GetChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class JavaFutureWithChannel
implements GetChannel,
IPending,
IBlockingDeref,
IDeref,
IType {
    public final Object fut;
    public final Object ch;

    public JavaFutureWithChannel(Object object, Object object2) {
        this.fut = object;
        this.ch = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"fut")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Future")})), (Object)Symbol.intern(null, (String)"ch"));
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
        JavaFutureWithChannel this_ = null;
        return ((Future)this_.fut).isDone();
    }

    public Object get_channel() {
        return this.ch;
    }
}

